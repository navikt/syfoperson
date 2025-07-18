![Build status](https://github.com/navikt/syfoperson/workflows/main/badge.svg?branch=master)

# Syfoperson
Syfoperson is a Ktor application written in Kotlin.
It is responsible for retrieving information about a person(Sykmeldt Arbeidstaker) 
for availabilty within the domain SYFO(Sykefraværsoppfølging).
The information retrieved by Syfoperson is available for frontend applications within the SYFO domain in Modia. 

## Technologies used

* Docker
* Gradle
* Kotlin
* Ktor
* Valkey

##### Test Libraries:

* Mockk
* JUnit

#### Requirements

* JDK 21

### Build

Run `./gradlew clean shadowJar`

### Pipeline
Pipeline is run with Github Action workflows.
Commits to Master-branch is deployed automatically to dev-gcp and prod-gcp.
Commits to non-master-branch is built without automatic deploy.

### Cache
This application uses Valkey on Aiven for caching.

### Lint (Ktlint)
##### Command line
Run checking: `./gradlew --continue ktlintCheck`

Run formatting: `./gradlew ktlintFormat`
##### Git Hooks
Apply checking: `./gradlew addKtlintCheckGitPreCommitHook`

Apply formatting: `./gradlew addKtlintFormatGitPreCommitHook`

## Contact

### For NAV employees

We are available at the Slack channel `#isyfo`.
