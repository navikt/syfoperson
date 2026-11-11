#!/usr/bin/env python3
"""Delvis konvertering av kommunale sysselsettingstall til SN2025-hovedområder."""

import argparse
from collections import Counter, defaultdict
import csv
import json
from pathlib import Path
import re
import sys
from urllib.error import URLError
from urllib.request import Request, urlopen

from sysselsetting import extract_rows, print_rows


TABLE_URL = "https://data.ssb.no/api/v0/no/table/13470"
KLASS_URL = "https://data.ssb.no/api/klass/v1"
DETAIL_CODE = re.compile(r"[0-9]{2}\.[0-9]{3}")


def fetch_json(url, query=None):
    request = Request(
        url,
        data=None if query is None else json.dumps(query).encode("utf-8"),
        headers={
            "Accept": "application/json",
            "Content-Type": "application/json",
            "User-Agent": "sysselsetting-sn2025-engangsuttrekk/1.0",
        },
    )
    with urlopen(request, timeout=60) as response:
        return json.load(response)


def create_query(metadata, kommune, year, grunnlag):
    variables = {variable["code"]: variable for variable in metadata["variables"]}
    codes = variables["NACE2007"]["values"]
    details = [code for code in codes if DETAIL_CODE.fullmatch(code)]
    if not details:
        raise ValueError("Tabellen mangler femsifrede SN2007-koder.")
    choices = {
        "Region": [kommune],
        "NACE2007": ["00-99", *details],
        "ContentsCode": ["SysselsatteArb" if grunnlag == "arbeidssted" else "Sysselsatte"],
        "Tid": [str(year)],
    }
    if set(choices) != set(variables):
        raise ValueError("SSB har endret tabellens variabler.")
    for code, values in choices.items():
        if not set(values).issubset(variables[code]["values"]):
            raise ValueError(f"Ugyldig valg for {code}: {', '.join(values)}")
    return {
        "query": [
            {"code": code, "selection": {"filter": "item", "values": values}}
            for code, values in choices.items()
        ],
        "response": {"format": "json-stat2"},
    }


def build_mapping(version, correspondence, old_version):
    if (
        version["id"] != 3218
        or old_version["id"] != 30
        or correspondence["sourceId"] != 3218
        or correspondence["targetId"] != 30
        or correspondence.get("changeTable") is not True
    ):
        raise ValueError("Uventede standardversjoner eller retning i koblingstabellen.")
    items = version["classificationItems"]
    by_code = {item["code"]: item for item in items}
    if len(by_code) != len(items):
        raise ValueError("Duplikate SN2025-koder.")
    sections = {
        item["code"]: item["name"]
        for item in items
        if str(item["level"]) == "1" and not item.get("parentCode")
    }
    if not sections:
        raise ValueError("SN2025-hovedområdene mangler.")
    roots = {}
    for code in by_code:
        current = code
        visited = set()
        while by_code[current].get("parentCode"):
            if current in visited:
                raise ValueError("Syklus i SN2025-hierarkiet.")
            visited.add(current)
            current = by_code[current]["parentCode"]
        if current not in sections:
            raise ValueError("SN2025-koden mangler et hovedområde.")
        roots[code] = current
    mapping = defaultdict(set)
    for item in correspondence["correspondenceMaps"]:
        old_code = item.get("targetCode")
        new_code = item.get("sourceCode")
        if old_code and DETAIL_CODE.fullmatch(old_code):
            # An unresolved target must prevent an otherwise partial mapping.
            mapping[old_code].add(roots.get(new_code))
    for item in old_version["classificationItems"]:
        code = item["code"]
        # The change table omits unchanged codes; follow their SN2025 parents.
        if DETAIL_CODE.fullmatch(code) and code not in mapping and code in roots:
            mapping[code].add(roots[code])
    return sections, mapping


def convert_rows(source_rows, sections, mapping):
    source = {row["kode"]: row for row in source_rows}
    if len(source) != len(source_rows):
        raise ValueError("Duplikate SN2007-koder i persontallene.")
    total = source["00-99"]["antall"]
    if total is None or total <= 0:
        raise ValueError("Mangler positiv publisert total.")
    counts = Counter()
    diagnostics = Counter()
    for code, row in source.items():
        if code == "00-99":
            continue
        if not DETAIL_CODE.fullmatch(code):
            raise ValueError(f"Uventet aggregert kildekode: {code}")
        count = row["antall"]
        if count is None or row["status"]:
            diagnostics["skjermet_eller_manglende"] += 1
            continue
        targets = mapping.get(code, set())
        if not targets or None in targets:
            diagnostics["uten_entydig_kobling"] += 1
        elif len(targets) != 1:
            diagnostics["flere_hovedomrader"] += 1
        else:
            section = next(iter(targets))
            if section not in sections:
                raise ValueError("Koblingen peker til et ukjent hovedområde.")
            counts[section] += count
            diagnostics["tilordnet"] += 1

    def row(code, name, count, status):
        return {
            "kode": code,
            "naring": name,
            "antall": count,
            "andel_prosent": count / total * 100,
            "status": status,
        }

    rows = [row("TOTAL", "Publisert total (SN2007)", total, "publisert_total")]
    rows.extend(
        row(code, name, counts[code], "delvis_konvertert")
        for code, name in sorted(sections.items())
    )
    rows.append(row(
        "REST",
        "Ikke tilordnet / avvik mot publisert total",
        total - sum(counts.values()),
        "rest_inkludert_avrundingsavvik",
    ))
    return rows, diagnostics


def write_csv(path, rows, metadata):
    fields = [*metadata, "kode", "naring", "antall", "andel_prosent", "status"]
    with path.open("w", encoding="utf-8-sig", newline="") as output:
        writer = csv.DictWriter(output, fieldnames=fields, delimiter=";")
        writer.writeheader()
        for row in rows:
            writer.writerow({
                **metadata,
                **row,
                "andel_prosent": f'{row["andel_prosent"]:.2f}'.replace(".", ","),
            })


def main(argv=None):
    parser = argparse.ArgumentParser(
        description=__doc__,
        epilog="Delsummer, ikke offisielle SN2025-tall. Ingen fordelingsvekter eller rekonstruksjon av skjermede celler.",
    )
    parser.add_argument("--kommune", default="4203", help="SSBs kommunekode (standard: 4203, Arendal)")
    parser.add_argument("--ar", type=int, default=2025, help="Statistikkår (standard: 2025)")
    parser.add_argument("--grunnlag", choices=("arbeidssted", "bosted"), default="arbeidssted")
    parser.add_argument("--csv", type=Path, help="Skriv også CSV (overskriver eksisterende fil)")
    args = parser.parse_args(argv)
    try:
        metadata = fetch_json(TABLE_URL)
        query = create_query(metadata, args.kommune, args.ar, args.grunnlag)
        dataset = fetch_json(TABLE_URL, query)
        version = fetch_json(f"{KLASS_URL}/versions/3218?language=nb")
        old_version = fetch_json(f"{KLASS_URL}/versions/30?language=nb")
        correspondence = fetch_json(f"{KLASS_URL}/correspondencetables/2919?language=nb")
        sections, mapping = build_mapping(version, correspondence, old_version)
        rows, diagnostics = convert_rows(extract_rows(dataset), sections, mapping)
        expected = set(query["query"][1]["selection"]["values"])
        actual = dataset["dimension"]["NACE2007"]["category"]["index"]
        if set(actual) != expected:
            raise ValueError("SSB returnerte ikke alle forespurte næringskoder.")
        region = next(variable for variable in metadata["variables"] if variable["code"] == "Region")
        name = region["valueTexts"][region["values"].index(args.kommune)]
        export_metadata = {
            "kommune": args.kommune,
            "kommunenavn": name,
            "ar": args.ar,
            "grunnlag": args.grunnlag,
            "standard": "SN2025_hovedomrader_delvis_konvertert",
            "kildestandard": "SN2007",
            "tabell": "13470",
            "koblingstabell": "2919",
            "metode": "Eksplisitte endringskoblinger, samt uendrede koder som finnes i begge standardversjoner",
            "oppdatert": dataset.get("updated", ""),
            "kobling_oppdatert": correspondence.get("lastModified", ""),
            "forbehold": (
                "Delsummer; skjermede og tvetydige koder er ikke fordelt; "
                "SSB justerer små tall; rest inkluderer avrundingsavvik"
            ),
        }
        if args.csv:
            write_csv(args.csv, rows, export_metadata)
        print(f"{name} ({args.kommune}), 4. kvartal {args.ar}, etter {args.grunnlag}")
        print("Sysselsatte personer 15–74 år. SN2025-hovedområder, delvis konvertert fra SN2007.")
        print("Antall og andeler er tilordnede delsummer, IKKE fullstendige eller offisielle SN2025-tall.")
        print("0 betyr ingen tilordnede personer, ikke nødvendigvis ingen sysselsatte i hovedområdet.")
        print("Andelene er beregnet av publisert total, inkludert uoppgitt næring.")
        print("Uendrede koder videreføres og grupperes etter SN2025-hierarkiet.")
        print(f"Kilde: https://www.ssb.no/statbank/table/13470 (oppdatert {export_metadata['oppdatert']})")
        print(f"Kobling: {KLASS_URL}/correspondencetables/2919\n")
        print_rows(rows)
        print("\nSN2007-detaljkoder:")
        print(f"  Entydig tilordnet: {diagnostics['tilordnet']}")
        print(f"  Skjermet eller manglende persontall: {diagnostics['skjermet_eller_manglende']}")
        print(f"  Kobling til flere hovedområder: {diagnostics['flere_hovedomrader']}")
        print(f"  Manglende/ufullstendig kobling: {diagnostics['uten_entydig_kobling']}")
        print("Resten inkluderer ikke-fordelte tall, uoppgitt næring og avrundingsavvik.")
        print("Resten er ikke et estimat for noen enkelt skjermet celle eller næring.")
        print("SSB erstatter persontall på 1 og 2 med 0 eller 3. Delsummer kan derfor avvike fra faktiske tall.")
        if rows[-1]["antall"] < 0:
            print("ADVARSEL: Delsummene overstiger totalen. Avviket kan skyldes SSBs justering av små tall.")
        if args.csv:
            print(f"CSV lagret i {args.csv}")
    except (URLError, TimeoutError, OSError, ValueError, KeyError, TypeError, IndexError) as error:
        print(f"Kunne ikke konvertere SSB-data: {error}", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
