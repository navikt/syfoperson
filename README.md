# syfoperson

## Om syfoperson
syfoperson er en spring-boot applikasjon skrevet i Kotlin. Den skal hente og lagre persondata på sykmeldte.


### Pipeline
https://jenkins-digisyfo.adeo.no/job/digisyfo/job/syfoperson/

### Cache

For caching brukes Redis. Redis pod må startes manuelt ved å kjøre følgdende kommando: `kubectl apply -f redis-config.yaml`.
