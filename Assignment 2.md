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
- Rules: Code changes always go through a branch and a pull request. Small edits to this report file were committed directly to main.
- Review rotation: TODO (fill in what really happened)

## 4. Progress in screenshots
Eclipse: all 11 tests of the skeleton are green (JUnit 6).

![Eclipse JUnit view: 11 of 11 tests green](Images/eclipse_tests.png)

GitHub Actions: runs of the workflows on our branches.

![GitHub Actions overview with all workflow runs](Images/actions_overview.png)

Red and green run on the branch `demo-red-ci`: run #6 failed because of a broken test, run #7 after the revert was green.

![Run 6 red and run 7 green](Images/red_green_run.png)

- TODO: branch list
- TODO: commit graph with three authors
- TODO: pull request with review comment

## 5. What we learned and what went wrong
- GitHub main was reset to an older commit and lost the merge of PR #25. We restored it with a pull request (PR #28) instead of a force push.
- Our local clone has GitLab as origin, so a plain `git pull` fetched the wrong state. We now use `git pull github main`.
- TODO: add after the BookingService and the reviews

---

## Own part: Patrick

Role: repository, CI/CD and integration.

- Built the project `hotel-app` (Maven, Java 21) with `Room`, `ReservationStatus`, `Reservation` and `ReservationStore` and 11 unit tests (PR #29). All tests are green in Eclipse (JUnit 6), see section 4.
- Pipeline: `.github/workflows/hotel-app-ci.yml` runs `mvn -B test` with Java 21 on every push and on every pull request to main. The workflow of the course stays unchanged.
- Red and green run: I broke a test on purpose on the branch `demo-red-ci`. Run #6 failed, after the revert run #7 was green. This shows that the pipeline reports a broken test before the code reaches main.
- Feature room search: `RoomSearchService` returns the rooms with enough capacity that are free in the period. Cancelled reservations do not block a room. 5 new tests, 16 in total, all green (PR #30).
  TODO: screenshot of PR #30 with the review
- Code changes go through an own branch and a pull request. PR #28 restored main after it was reset (section 5). PR #29 (skeleton) and #31 (report file) were merged without review, and small edits to this report were committed directly to main. Code changes get a review.
- I used Claude Code for writing code. It never commits, pushes or merges, I run the tests and do the Git steps myself.
- Still to do: BookingService (`hotel.booking`) after the features of Marin and Seungheon are merged.

## Own part: Marin
TODO

## Own part: Seungheon
TODO
