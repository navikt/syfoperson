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
* Redis

##### Test Libraries:

* Kluent
* Mockk
* Spek
#### Requirements

* JDK 17

### Build

Run `./gradlew clean shadowJar`

### Pipeline
Pipeline is run with Github Action workflows.
Commits to Master-branch is deployed automatically to dev-gcp and prod-gcp.
Commits to non-master-branch is built without automatic deploy.

### Cache
This application uses Redis for caching. A single Redis pod is responsible for caching. Redis is deployed automatically on changes to workflow or config on master
branch. For manual deploy, run: `kubectl apply -f .nais/redis-config.yaml`

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
