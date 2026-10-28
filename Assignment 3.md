Team: Exchange student
2601764 Seungheon, Sa
2601783 Wasier, Marin
2601760 Hein, Patrick

# Assignment 3 (Docker): Report

Status: plan. Sections 1 to 4 describe what we agreed to do. `[FILL IN]` is replaced with the real result after each step.

## 1. Application and image
- Application: `hotel-app` (Java 21, Maven) from Assignment 2. New: a small console program `hotel.app.Main` that shows the existing features (room search, booking, reservation, user accounts). The app had no entry point before, so no image could run it.
- Dockerfile: multi-stage build as in Example 8-4 (Maven image builds the JAR, Java 21 JRE image runs it). `src` and `pom.xml` are copied into the runtime image, so everybody can pull the image and take the source code out of it (`docker cp`) to develop further.
- Docker Hub repository: `[FILL IN: username/image_name]`
- Platform: GitHub, same repository as Assignment 2 (`marinw1816-cyber/Project-hotel-room`)

## 2. Who does what
| Step | Person | Task | Branch | Reviewer (merges) | Image tag |
|---|---|---|---|---|---|
| 0 | Marin | Branch rule on main (review required, no force push), Docker Hub access for the team | none | none | none |
| 0 | all | Docker installed, `docker run hello-world`, Docker Hub account | none | none | none |
| 1 | Patrick | `Main`, `mainClass` in `pom.xml`, Dockerfile, `.dockerignore`, build, run, first push | `feature-docker` | Seungheon | `v1` |
| 2 | Marin | Pull `v1`, run it, take the source out of the image, add the reservation demo, build, push | `feature-docker-reservation` | Patrick | `v2` |
| 3 | Seungheon | Pull `v2`, run it, add the user demo, build, push | `feature-docker-user` | Marin | `v3` |
| 4 | Patrick | Pull `v3`, check that all three parts run, tag `latest`, collect screenshots, write the report | `report-docker` | Marin | `latest` |

Order of the chain: each person starts only after the previous pull request is merged. The image is always built from the merged `main`, so the image and `main` show the same code.

## 3. Tasks in detail

### Patrick: Dockerfile and first image
1. Branch `feature-docker` from current `main`.
2. Add `hotel/app/Main.java` in `hotel-app`: console demo with a room list, a search for free rooms and one booking (uses `hotel.search` and `hotel.booking`). It prints the result and exits.
3. Set `mainClass` in the `maven-jar-plugin` of `hotel-app/pom.xml`, so `java -jar` starts the app.
4. Add `Dockerfile` and `.dockerignore` in `hotel-app`.
5. Build and run locally: `docker build -t <user>/<image>:v1 .` and `docker run --rm <user>/<image>:v1`.
6. Pull request with description. Pipeline `hotel-app CI` must be green. Seungheon reviews and merges.
7. After the merge: `git pull github main`, build, `docker push <user>/<image>:v1`.

### Marin: pull, change, push (reservation)
1. Pull `v1` from Docker Hub and run it.
2. Take the source out of the pulled image (`docker create` and `docker cp`, or a shell in the container) and show that it is the same code as on `main`.
3. Branch `feature-docker-reservation` from current `main`. Add a `ReservationDemo` in `hotel.reservation`: create a reservation, confirm it, change the dates, print the total price (uses `ReservationService`). `Main` gets one line that calls it.
4. Add or adjust a test if the change needs one. Pull request, Patrick reviews and merges.
5. After the merge: `git pull github main`, build, run, `docker push <user>/<image>:v2`.

### Seungheon: pull, change, push (user accounts)
1. Pull `v2` from Docker Hub and run it.
2. Branch `feature-docker-user` from current `main`. Add a `UserDemo` in `hotel.user`: register a user, log in with a right and a wrong password, print the role (uses `UserService`). `Main` gets one line that calls it.
3. Add or adjust a test if the change needs one. Pull request, Marin reviews and merges.
4. After the merge: `git pull github main`, build, run, `docker push <user>/<image>:v3`.

### Patrick: check and report (step 4)
- Pull `v3`, run it, check that search, booking, reservation and user demo all appear in the output.
- Tag `latest`, write this report, collect the screenshots listed in section 5.

## 4. Rules
- Code changes only through a branch and a pull request, never directly on main, never `--force`.
- The author does not merge his own pull request. The reviewer merges after the review.
- Every change to `Main` is one line per person, to avoid merge conflicts. The demo code lives in the own package.
- Tokens and passwords never go into the repository, the report or a chat.
- If a step is not done by `[FILL IN: date agreed in the team]`, Patrick does it and the report says so.

## 5. Evidence (screenshots, with caption)
- Patrick: Dockerfile in the pull request, `docker build` and `docker run` output, Docker Hub tags page, final `docker pull` of `v3`.
- Marin: `docker pull` of `v1`, source files taken out of the image, pull request with review, `docker push` of `v2`.
- Seungheon: `docker pull` of `v2`, pull request with review, `docker push` of `v3`.

---

## Own part: Patrick
`[FILL IN]`

## Own part: Marin
`[FILL IN]`

## Own part: Seungheon
`[FILL IN]`
