# Jenkins with Docker CLI and Docker Compose
# Based on the course (XII. Jenkins, "Adding Docker inside Jenkins", Option 2)
FROM jenkins/jenkins:lts

USER root

# Docker CLI from the Debian package, like in the course
RUN apt-get update && apt-get install -y docker.io curl && apt-get clean

# Docker Compose v2 plugin
# Course downloads only x86_64; uname -m picks aarch64 (Mac with Apple chip) or x86_64 (Windows)
RUN mkdir -p /usr/libexec/docker/cli-plugins && \
    curl -fSL "https://github.com/docker/compose/releases/latest/download/docker-compose-linux-$(uname -m)" \
    -o /usr/libexec/docker/cli-plugins/docker-compose && \
    chmod +x /usr/libexec/docker/cli-plugins/docker-compose

USER jenkins
