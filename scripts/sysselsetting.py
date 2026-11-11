#!/usr/bin/env python3
"""Hent sysselsatte personer etter SN2007 fra SSB-tabell 07984."""

import argparse
import csv
import json
import sys
from pathlib import Path
from urllib.error import URLError
from urllib.request import Request, urlopen


TABLE = "07984"
API_URL = f"https://data.ssb.no/api/v0/no/table/{TABLE}"


def fetch_json(query=None):
    payload = None if query is None else json.dumps(query).encode("utf-8")
    request = Request(
        API_URL,
        data=payload,
        headers={
            "Accept": "application/json",
            "Content-Type": "application/json",
            "User-Agent": "sysselsetting-engangsuttrekk/1.0",
        },
    )
    with urlopen(request, timeout=60) as response:
        return json.load(response)


def create_query(metadata, kommune, year, grunnlag):
    variables = {variable["code"]: variable for variable in metadata["variables"]}
    metric = "SysselsatteArb" if grunnlag == "arbeidssted" else "Sysselsatte"
    choices = {
        "Region": [kommune],
        "NACE2007": variables["NACE2007"]["values"],
        "Kjonn": ["0"],
        "Alder": ["15-74"],
        "ContentsCode": [metric],
        "Tid": [str(year)],
    }
    if set(choices) != set(variables):
        raise ValueError("SSB har endret tabellens variabler. Skriptet må oppdateres.")
    for code, values in choices.items():
        invalid = set(values) - set(variables[code]["values"])
        if invalid:
            detail = ""
            if code == "Tid":
                detail = " Tilgjengelige år: " + ", ".join(variables[code]["values"])
            raise ValueError(f"Ugyldig valg for {code}: {', '.join(sorted(invalid))}.{detail}")
    return {
        "query": [
            {"code": code, "selection": {"filter": "item", "values": values}}
            for code, values in choices.items()
        ],
        "response": {"format": "json-stat2"},
    }


def indexed_value(values, index):
    if isinstance(values, dict):
        return values.get(str(index))
    return values[index]


def extract_rows(dataset):
    dimensions = dataset["id"]
    sizes = dataset["size"]
    if len(dimensions) != len(sizes) or "NACE2007" not in dimensions:
        raise ValueError("Uventede dimensjoner i SSB-responsen.")
    if any(size != 1 for code, size in zip(dimensions, sizes) if code != "NACE2007"):
        raise ValueError("Forventet ett valg i alle dimensjoner unntatt næring.")
    category = dataset["dimension"]["NACE2007"]["category"]
    index = category["index"]
    if isinstance(index, list):
        index = {code: position for position, code in enumerate(index)}
    if sorted(index.values()) != list(range(sizes[dimensions.index("NACE2007")])):
        raise ValueError("Uventet indeks for næringskodene i SSB-responsen.")
    counts = {}
    for code, position in index.items():
        status = indexed_value(dataset.get("status", {}), position)
        value = indexed_value(dataset["value"], position)
        if value is not None and (
            not isinstance(value, (int, float))
            or isinstance(value, bool)
            or value < 0
            or not float(value).is_integer()
        ):
            raise ValueError(f"Uventet antall personer for næring {code}.")
        counts[code] = (int(value) if value is not None else None, status)
    total, total_status = counts["00-99"]
    if total is None or total <= 0 or total_status:
        raise ValueError("Totalen mangler, er merket eller er null. Kan ikke beregne andeler.")
    rows = []
    for code, position in sorted(index.items(), key=lambda item: item[1]):
        count, status = counts[code]
        available = count is not None and not status
        rows.append({
            "kode": code,
            "naring": category["label"][code],
            "antall": count if available else None,
            "andel_prosent": count / total * 100 if available else None,
            "status": status or ("" if available else "mangler"),
        })
    return rows


def print_rows(rows):
    width = max(len("Næring"), *(len(row["naring"]) for row in rows))
    print(f"{'Kode':<7} {'Næring':<{width}} {'Personer':>13} {'Andel':>10}")
    for row in rows:
        count = "utilgjengelig" if row["antall"] is None else f'{row["antall"]:,}'.replace(",", " ")
        share = "-" if row["andel_prosent"] is None else f'{row["andel_prosent"]:.2f} %'.replace(".", ",")
        print(f'{row["kode"]:<7} {row["naring"]:<{width}} {count:>13} {share:>10}')


def write_csv(path, rows, kommune, name, year, grunnlag, updated):
    metadata = {
        "kommune": kommune,
        "kommunenavn": name,
        "ar": year,
        "grunnlag": grunnlag,
        "standard": "SN2007",
        "tabell": TABLE,
        "oppdatert": updated,
    }
    fields = [*metadata, "kode", "naring", "antall", "andel_prosent", "status"]
    with path.open("w", encoding="utf-8-sig", newline="") as output:
        writer = csv.DictWriter(output, fieldnames=fields, delimiter=";")
        writer.writeheader()
        for row in rows:
            formatted = dict(row)
            if row["andel_prosent"] is not None:
                formatted["andel_prosent"] = f'{row["andel_prosent"]:.2f}'.replace(".", ",")
            writer.writerow({**metadata, **formatted})


def main(argv=None):
    parser = argparse.ArgumentParser(
        description=__doc__,
        epilog=(
            "Ingen SN2025-konvertering: koblingstabellen mangler fordelingsvekter. "
            "Eksempel: python3 scripts/sysselsetting.py --kommune 4203 --ar 2025 "
            "--grunnlag arbeidssted --csv arendal-2025.csv"
        ),
    )
    parser.add_argument("--kommune", default="4203", help="SSBs kommunekode (standard: 4203, Arendal)")
    parser.add_argument("--ar", type=int, default=2025, help="Statistikkår (standard: 2025)")
    parser.add_argument("--grunnlag", choices=("arbeidssted", "bosted"), default="arbeidssted")
    parser.add_argument("--csv", type=Path, help="Skriv også CSV til denne filen (overskriver eksisterende fil)")
    args = parser.parse_args(argv)
    try:
        metadata = fetch_json()
        query = create_query(metadata, args.kommune, args.ar, args.grunnlag)
        dataset = fetch_json(query)
        rows = extract_rows(dataset)
        region = next(variable for variable in metadata["variables"] if variable["code"] == "Region")
        name = region["valueTexts"][region["values"].index(args.kommune)]
        updated = dataset.get("updated", "ikke oppgitt")
        if args.csv:
            write_csv(args.csv, rows, args.kommune, name, args.ar, args.grunnlag, updated)
        print(f"{name} ({args.kommune}), 4. kvartal {args.ar}, etter {args.grunnlag}")
        print("SN2007, 17 næringsgrupper. Ikke konvertert til SN2025.")
        print("Sysselsatte personer 15–74 år, begge kjønn. Ikke årsverk eller antall arbeidsforhold.")
        print("Andelene bruker SSBs publiserte total, inkludert uoppgitt næring.")
        print(f"Kilde: https://www.ssb.no/statbank/table/{TABLE}")
        print(f"Sist oppdatert hos SSB: {updated}\n")
        print_rows(rows)
        print("\nUtilgjengelige tall er ikke null. ':' betyr skjermet av konfidensialitetshensyn.")
        print("SSB justerer små tall av personvernhensyn. Avrunding og skjerming kan gi avvik fra totalen.")
        if args.csv:
            print(f"CSV lagret i {args.csv}")
    except (URLError, TimeoutError, OSError, ValueError, KeyError, TypeError, IndexError) as error:
        print(f"Kunne ikke hente eller behandle SSB-data: {error}", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
