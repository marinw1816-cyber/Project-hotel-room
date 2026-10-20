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

Role: repository, CI/CD and integration.

- Built the project `hotel-app` (Maven, Java 21) with `Room`, `ReservationStatus`, `Reservation` and `ReservationStore` and 11 unit tests (PR #29). All tests are green in Eclipse (JUnit 6).
  <img width="1568" height="628" alt="image" src="https://github.com/user-attachments/assets/e9a8a3ac-cc02-4ead-8fa0-33f642173b5b" />
- Pipeline: `.github/workflows/hotel-app-ci.yml` runs `mvn -B test` with Java 21 on every push and on every pull request to main. The workflow of the course stays unchanged.
  [Screenshot: Images/actions_overview.png]
- Red and green run: I broke a test on purpose on the branch `demo-red-ci`. Run #6 failed, after the revert run #7 was green. This shows that the pipeline stops a broken test before it reaches main. <img width="2638" height="664" alt="image" src="https://github.com/user-attachments/assets/b3c1f38d-af02-40bc-9623-7bd5bab6899b" />

- Feature room search: `RoomSearchService` returns the rooms with enough capacity that are free in the period. Cancelled reservations do not block a room. 5 new tests, 16 in total, all green (PR #30).
  [Screenshot: Images/pr_30.png]
- Own branch for every change, no push to main. PR #28 restored main after it was reset (section 5). PR #29 (skeleton) and #31 (report file) were merged without review, code changes get a review.
- I used Claude Code for writing code. It never commits, pushes or merges, I run the tests and do the Git steps myself.
- Still to do: BookingService (`hotel.booking`) after the features of Marin and Seungheon are merged.

## Own part: Marin
TODO

## Own part: Seungheon
TODO
