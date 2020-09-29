# syfoperson

## Om syfoperson
syfoperson er en spring-boot applikasjon skrevet i Kotlin. Den skal hente og lagre persondata på sykmeldte.


### Pipeline
Pipeline er på Github Action.
Commits til Master-branch deployes automatisk til dev-fss og prod-fss.
Commits til ikke-master-branch bygges uten automatisk deploy.

### Cache

For caching brukes Redis. Redis pod må startes manuelt ved å kjøre følgdende kommando: `kubectl apply -f redis-config.yaml`.

### Lint
Kjør `./gradlew --continue ktlintCheck`
