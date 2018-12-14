# syfoperson

## Om syfoperson
syfoperson er en spring-boot applikasjon skrevet i Kotlin. Den skal hente og lagre persondata på sykmeldte, samt lagre
data om knytninger mellom veiledere og sykmeldte. Tjenester som eksponeres skal konsumeres av syfooversikt.

## Database
Appen kjører med en lokal H2 in-memory database. Den spinnes opp som en del av applikasjonen og er 
også tilgjengelig i tester. Du kan logge inn og kjøre spørringer på:
`localhost/h2` med jdbc_url: `jdbc:h2:mem:testdb`