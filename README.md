# Hotel Room Reservation – DevOps Course

Team project of the DevOps course: a hotel room reservation application with different features for
**customers**, **receptionists** and **administrators**, built with a CI pipeline and delivered as a Docker image.

**Team: Exchange student**

|Name|Role in the project|
|-|-|
|Patrick Hein|Repository, CI/CD, integration, room search and booking|
|Marin Wasier|Reservations (confirm, change dates, total price), branch rules|
|Seungheon Sa|User accounts (sign-up, sign-in, roles, password hashing)|

## Project structure

|Path|Content|
|-|-|
|[`hotel-app/`](hotel-app)|The application (Java 21, Maven, JUnit), its `Dockerfile` and `.dockerignore`|
|[`.github/workflows/hotel-app-ci.yml`](.github/workflows/hotel-app-ci.yml)|CI pipeline: runs all tests on every push and every pull request to `main`|
|[`Images/`](Images)|Diagrams and screenshots used in the reports|
|[`Example\_101/`](Example_101)|Example project of the course (not part of the hotel app)|

Packages in `hotel-app/src/main/java/hotel`:

|Package|What it does|
|-|-|
|`model`|`Room`, `Reservation`, `ReservationStatus`, `ReservationStore`|
|`search`|Find the free rooms for a period and a number of guests|
|`booking`|Book the cheapest free room that is big enough|
|`reservation`|Confirm a reservation, change its dates without double booking, total price|
|`user`|Sign-up, sign-in, roles (`CUSTOMER`, `RECEPTIONIST`, `ADMIN`), hashed passwords|
|`app`|`Main`: console demo of all features|

## Reports

* [Assignment 1 – Requirements](Assignment%201.md)
* [Assignment 2 – CI/CD](Assignment%202.md)
* [Assignment 3 – Docker](Assignment%203.md)

## Run the tests

Requirements: Java 21 and Maven.

```bash
cd hotel-app
mvn -B test
```

Without Java or Maven installed, the tests can also run in Docker (PowerShell):

```powershell
docker run --rm -v "${PWD}\\hotel-app:/app" -w /app maven:3.9.6-eclipse-temurin-21 mvn -B test
```

## Run the application with Docker

The image is on Docker Hub: [`patrickhein1008/hotel-app`](https://hub.docker.com/r/patrickhein1008/hotel-app)

```bash
docker pull patrickhein1008/hotel-app:v2
docker run --rm patrickhein1008/hotel-app:v2
On a Mac with Apple Silicon (M1/M2/M3), `v2` does not run directly because it was built on an x86\_64 laptop and is only available for `linux/amd64`. Add `--platform linux/amd64` to both commands, Docker Desktop then runs it with emulation:



```bash

docker pull --platform linux/amd64 patrickhein1008/hotel-app:v2

docker run --rm --platform linux/amd64 patrickhein1008/hotel-app:v2

```

Build the image yourself from the source:

```bash
cd hotel-app
docker build -t hotel-app .
docker run --rm hotel-app
```

## How we work

* `main` is protected: every change goes through a branch and a pull request.
* A pull request needs **1 approval** from another team member and a green **`Test hotel-app`** check before it can be merged.
* Force pushes and deleting `main` are blocked, also for admins.
* The author does not merge their own pull request: the reviewer merges.
* Docker images are always built from the merged `main`, so the image and `main` contain the same code.

