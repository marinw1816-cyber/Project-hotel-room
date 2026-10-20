Team: Exchange student
2601764 Seungheon, Sa
2601783 Wasier, Marin
2601760 Hein, Patrick

# Assignment 2 (CI/CD): Report

## 1. Team and choice of language
- Language: Java 21 with Maven and JUnit 6
- Reason: the CI pipeline of the course already runs Maven, JDK 21 and Eclipse are set up for all three of us
- Tools: Eclipse as IDE, GitHub for pull requests and Actions

## 2. Who does what
| Person | Role | Feature | Package |
|---|---|---|---|
| Patrick | Repo, CI/CD, integration | Room search, later BookingService | hotel.search, hotel.booking |
| Marin | Reservation | Confirm, change dates, total price | hotel.reservation |
| Seungheon | User accounts | Registration, login, roles, hashed password | hotel.user |

## 3. Repository structure and branch rules
- Repo: marinw1816-cyber/Project-hotel-room
- Folder `hotel-app` (Maven project). `Example_101` and the workflow of the course stay untouched.
- Own workflow: `.github/workflows/hotel-app-ci.yml` (runs `mvn -B test` on every push and on pull requests to main)
- Rules: own branch from main, never push to main directly, a pull request needs a green check and a review before the merge
- Review rotation: TODO (fill in what really happened)

## 4. Progress in screenshots
- TODO: branch list
- TODO: commit graph with three authors
- TODO: pull request with review comment
- TODO: green pipeline
- TODO: red run #6, then green run #7
- TODO: Eclipse test run (full window)

## 5. What we learned and what went wrong
- GitHub main was reset to an older commit and lost the merge of PR #25. We restored it with a pull request (PR #28) instead of a force push.
- Our local clone has GitLab as origin, so a plain `git pull` fetched the wrong state. We now use `git pull github main`.
- TODO: add after the BookingService and the reviews

---

## Own part: Patrick
TODO

## Own part: Marin
TODO

## Own part: Seungheon
TODO
