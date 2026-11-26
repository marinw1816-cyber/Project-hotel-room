Team: Exchange student
2601764 Seungheon, Sa
2601783 Wasier, Marin
2601760 Hein, Patrick

# Assignment 5 (Jenkins): Report

Status: [FILL IN at the end]. Part 1 to 4 is what we built, part 5 and the own parts show what we actually did and tested.

## 1. Application, Jenkins and Jenkinsfile
- Application: the same `hotel-app` as in Assignment 4 (Spring Boot, Java 21, Maven, MariaDB, room API under `/api/rooms`, 51 tests with H2).
- Jenkins runs as a Docker container on each of our laptops (Patrick: Mac with Apple chip, Marin and Seungheon: Windows).
- The Jenkins image is built from `jenkins/Dockerfile`: the official `jenkins/jenkins:lts` plus Docker CLI and Docker Compose, like option 2 in the course ("Adding Docker inside Jenkins").
- The pipeline is in `Jenkinsfile` in the root of the repo. Jenkins reads it from GitHub ("Pipeline script from SCM"), so the pipeline is versioned in Git like the code.
- Jenkins job: `hotel-app-pipeline`, repository `https://github.com/marinw1816-cyber/Project-hotel-room.git`, branch `*/main` (during a review the branch of the PR), Script Path `Jenkinsfile`. The repo is public, so no Git credentials are needed.

## 2. Who does what
| Step | Person | Task | Branch | Reviewer (merges) |
|---|---|---|---|---|
| 1 | Patrick | Jenkins image with Docker CLI, Jenkins container, Jenkinsfile with Checkout, Build, Test | `feature-jenkins` | Marin |
| 2 | Marin | CD part: stages Docker Build and optional Docker Push with Jenkins credentials | `jenkins-docker-image` | Patrick |
| 3 | Seungheon | Integration test with Docker Compose, proof "red and green" | `jenkins-compose-test` | Patrick |
| 4 | Patrick | Report | `report-jenkins` | Marin |

Plan vs. reality:
- First plan: Seungheon reviews step 1, Marin reviews step 3. To save time we changed it on 23.11.: Marin reviewed step 1, because he needed his own Jenkins for step 2 anyway.
- Step 1: PR #63, Marin tested it on his own Jenkins (Windows), approved and merged it on 24.11.
- Step 2: PR #64, Patrick tested it on his own Jenkins (Mac), approved and merged it on 24.11.
- Step 3: [FILL IN]

## 3. Our Jenkinsfile
Declarative pipeline (`pipeline`, `agent`, `options`, `parameters`, `environment`, `triggers`, `stages`, `post`), like in the course.

- `agent any`: the pipeline runs on our Jenkins.
- `options`: `skipDefaultCheckout(true)`, because we do the checkout in our own stage, so it is visible in the stage view. `buildDiscarder` keeps the last 10 builds.
- `parameters`: `PUSH_IMAGE` (default false) and `DOCKERHUB_REPO` (default `marin971/hotel-app`). You see them under "Build with Parameters".
- `environment`: `IMAGE_NAME = hotel-app`, `IMAGE_TAG` = Jenkins build number, so every build has its own image.
- `triggers`: `pollSCM('H/5 * * * *')`. Jenkins checks GitHub about every 5 minutes and starts a build when there is a new commit.
- Stages:
  - **Checkout**: `checkout scm` gets the code from the repo and branch of the job.
  - **Build**: `mvn -B clean compile` in the folder `hotel-app`.
  - **Test**: `mvn -B test`, then `junit` in `post { always }` saves the test results in Jenkins (also when a test fails).
  - Build and Test run in a Docker agent `maven:3.9.6-eclipse-temurin-21` with `reuseNode true` (same workspace). The Maven repository is in the workspace (`-Dmaven.repo.local=$WORKSPACE/.m2/repository`), because the user `jenkins` can't write to `/root`.
  - **Docker Build**: `docker.build("hotel-app:<build number>", 'hotel-app')` from the Docker Pipeline plugin, uses `hotel-app/Dockerfile`. Then the same image also gets the tag `latest`.
  - **Docker Push**: only when `PUSH_IMAGE` is ticked (`when`). User and access token come from the Jenkins credentials `dockerhub` with `withCredentials`, login with `--password-stdin`, push of the build number tag and `latest`, `docker logout` in `post`.
  - [FILL IN Seungheon: Integration test stage]
- `post`: message for success or failure.

## 4. How to start Jenkins
In the root of the repo (Docker Desktop running):
```
docker build -t myjenkins-docker:latest jenkins
docker run -d -p 9000:8080 -p 50000:50000 --name jenkins --group-add 0 --restart unless-stopped -v jenkins_home:/var/jenkins_home -v /var/run/docker.sock:/var/run/docker.sock myjenkins-docker:latest
docker exec jenkins docker ps
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```
- Port 9000 for Jenkins, because 8080 is our app.
- `jenkins_home` volume: jobs, plugins, users and build history stay when the container is removed.
- `/var/run/docker.sock`: Jenkins uses the Docker engine of the laptop, so it can start the Maven container and build our app image.
- `--group-add 0` and `--restart unless-stopped`: not in the course, see part 6.
- Then in the browser `http://localhost:9000`: unlock with the password (not shown here), "Install suggested plugins", own admin user, plugin "Docker Pipeline", new item `hotel-app-pipeline` as Pipeline with "Pipeline script from SCM".

## 5. Testing (screenshots)
Times in Jenkins are UTC (Finland is UTC+2), because the container has no time zone set.

### Step 1 (Patrick, Mac, 23.11.)
![Building the Jenkins image with Docker CLI and Docker Compose](Images/a5_patrick_01_build.png)
![First try: permission denied on the Docker socket for the user jenkins, socket belongs to group 0](Images/a5_patrick_02_permission_denied.png)
![Container started again with --group-add 0: jenkins is in group 0, docker ps works inside Jenkins](Images/a5_patrick_03_group_add.png)
![Setup wizard: installing the suggested plugins](Images/a5_patrick_05_plugins.png)
![Docker Pipeline plugin installed](Images/a5_patrick_06_docker_pipeline.png)
![Job hotel-app-pipeline: Pipeline script from SCM, GitHub repo, branch feature-jenkins, Script Path Jenkinsfile](Images/a5_patrick_07_job_config.png)
![Build #1: Checkout, Build, Test and Post Actions green](Images/a5_patrick_08_stage_view.png)
![Console of build #1: Tests run 51, BUILD SUCCESS, Maven container stopped and removed, Finished: SUCCESS](Images/a5_patrick_09_console.png)
![Test result: 51 tests in 7 packages, all passing](Images/a5_patrick_10_test_result.png)
![Trigger "Poll SCM" with H/5 * * * * comes from the Jenkinsfile, not set by hand](Images/a5_patrick_11_poll_scm.png)
![PR #63 with my description, merged by Marin after his approve and his test on Windows](Images/a5_patrick_12_pr63.png)

Marin's test of step 1 on Windows (review of PR #63):
![docker ps inside Jenkins works on Windows too](Images/a5_marin_docker_ps_in_jenkins.png)
![Build #1 on Marin's Jenkins: all stages green](Images/a5_marin_pipeline_green.png)
![51 tests passing](Images/a5_marin_test_result_51.png)

### Step 2 (Marin, Windows)
![Build #2: Docker Build green, Docker Push skipped (PUSH_IMAGE false)](Images/a5_marin_pipeline_docker_build.png)
![hotel-app:2 and hotel-app:latest with the same image ID](Images/a5_marin_image_ls.png)
![Build #3 with PUSH_IMAGE ticked: Docker Push green](Images/a5_marin_pipeline_docker_push.png)
![Docker Hub: marin971/hotel-app with tags 3 and latest, same digest](Images/a5_marin_dockerhub_tags.png)

My review of step 2 (Patrick, Mac, 24.11.):
![Build #2 on branch jenkins-docker-image: Docker Build green, Docker Push skipped](Images/a5_patrick_13_stage_view_docker.png)
![docker image ls in the console: hotel-app:2 and latest have the same ID, push skipped](Images/a5_patrick_14_docker_images.png)
![My approve on PR #64 and the merge](Images/a5_patrick_15_pr64_review.png)

After both merges, build #3 on main (Patrick, Mac, 24.11.):
![Console: Jenkinsfile obtained from Git, checkout of main 24a3a72, Maven container starts](Images/a5_patrick_16_build_main_checkout.png)
![Console: image hotel-app:3, push skipped, Finished: SUCCESS](Images/a5_patrick_17_build_main_result.png)

### Step 3 (Seungheon, Windows)
[FILL IN]

### Automatic build after a merge
[FILL IN: build started by an SCM change after the next merge on main]

## 6. Differences from the course and problems
- **Java 21 instead of 17:** the course uses `maven:3.9.5-eclipse-temurin-17`, our app needs Java 21, so we use `maven:3.9.6-eclipse-temurin-21` (the same image as in our Dockerfile).
- **App in a subfolder:** Maven runs in `hotel-app`, the JUnit path is `hotel-app/target/surefire-reports/*.xml`.
- **Docker Compose on the Mac:** the course downloads `docker-compose-linux-x86_64`. That doesn't run on the Apple chip, so the Dockerfile chooses the file with `uname -m` (`aarch64` or `x86_64`).
- **Poll SCM instead of a webhook:** the course shows "Build periodically" and the GitHub webhook. A webhook from GitHub can't reach Jenkins on `localhost`, so we use Poll SCM. It is in the Jenkinsfile, not only in the job settings.
- **Credentials ID:** the course uses `docker-hub-creds`, we use `dockerhub`. Same idea: the token is only in the Jenkins credentials store, not in Git.
- **Socket permission:** the course tests Docker inside Jenkins with `docker exec -u root`. The pipeline runs as user `jenkins`, and for him we got "permission denied". The socket belongs to group 0 with read and write rights, so `--group-add 0` fixed it. Marin and Seungheon use the same `docker run` command on Windows.
- **Jenkins stopped after a restart:** after the plugin install Jenkins restarted itself and the container stayed "Exited (5)". Jenkins in Docker needs a restart policy, so we added `--restart unless-stopped` (`docker update` for the existing container).
- **Poll SCM after changing the branch:** after the merges we changed the job from the PR branch to `*/main`, but no build started. The polling log showed that Jenkins still compared with the last built branch (`jenkins-docker-image`, already deleted). After one manual build on `main` the polling uses `main`.
- **Not in the report:** the unlock page with the initial admin password. The password is only in the container and not shown here.

## 7. Rules
- Own branch from `main`, PR with a description, CI (`Test hotel-app`) has to be green.
- The reviewer runs the branch on his own Jenkins before he approves. The author doesn't merge his own PR.
- No passwords or tokens in the Jenkinsfile, the repo or the report. Docker Hub token only in the Jenkins credentials.
- The integration test has to use its own Compose project name, so it never touches our local stack `hotel-compose`.

## Own part: Patrick
Role: step 1, review of Marin's step 2, report.

- **Jenkins image (PR #63):** I wrote `jenkins/Dockerfile` like option 2 in the course, but the Compose download uses `uname -m`, because my Mac has an Apple chip. Build and run like in part 4.
- **Docker inside Jenkins:** my first `docker exec jenkins docker ps` failed with "permission denied". I checked the socket (`ls -ln`: owner 0, group 0, `rw` for the group) and the user (`id`: only group 1000). With `--group-add 0` it worked. I tested without `-u root` on purpose, because the pipeline runs as `jenkins`.
- **Jenkinsfile:** Checkout, Build and Test, Maven in a Docker agent with Java 21, `junit` in `post`, Poll SCM in the file. Build #1 on `feature-jenkins` was green, 51 tests.
- **Problems:** the container stopped after the plugin restart (fixed with `--restart unless-stopped`), and Poll SCM didn't start a build after I changed the job to `main` (fixed with one manual build, see part 6).
- **Review of step 2 (PR #64):** I changed my job to `*/jenkins-docker-image` and ran it: build #2 green, `hotel-app:2` and `latest` with the same image ID, push skipped. I checked that no secret is in the Jenkinsfile (`withCredentials`, `--password-stdin`, script in single quotes). In the review I asked Marin to run the push once and mentioned the credentials ID. Then I approved and merged.
- After both merges build #3 on `main` (`24a3a72`) was green and created `hotel-app:3`.

## Own part: Marin
[FILL IN]

## Own part: Seungheon
[FILL IN]
