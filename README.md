![Build status](https://github.com/navikt/syfoperson/workflows/main/badge.svg?branch=master)

# Syfoperson
Syfoperson is a Spring Boot application written in Kotlin.
It is responsible for retrieving information about a person(Sykmeldt Arbeidstaker) 
for availabilty within the domain SYFO(Sykefraværsoppfølging).
The information retrieved by Syfoperson is made available for frontend application wihtin SYFO and for GCP application within SYFO. 

## Technologies Used
* Docker
* Gradle
* Kotlin
* Spring Boot
* Redis
* Postgres
* Vault

### Pipeline
Pipeline is run with Github Action workflows.
Commits to Master-branch is deployed automatically to dev-fss and prod-fss.
Commits to non-master-branch is built without automatic deploy.

### Cache
A single Redis pod is responsible for caching.
To deploy the redis pod run: `kubectl apply -f .nais/redis-config.yaml`.

### Lint (Ktlint)
##### Command line
Run checking: `./gradlew --continue ktlintCheck`

Run formatting: `./gradlew ktlintFormat`
##### Git Hooks
Apply checking: `./gradlew addKtlintCheckGitPreCommitHook`

Apply formatting: `./gradlew addKtlintFormatGitPreCommitHook`
