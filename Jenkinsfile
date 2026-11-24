// Jenkins pipeline for hotel-app (Assignment 5)
// Based on the course (XII. Jenkins, Example 9-1). Differences:
// - Maven runs in a Docker container with Java 21 (course uses Java 17, our app needs 21)
// - the app is in the subfolder hotel-app, so Maven runs there
// - CD part: build the Docker image of the app, optional push to Docker Hub
pipeline {
    agent any

    options {
        // we do the checkout ourselves in the stage "Checkout", so it shows up in the Stage View
        skipDefaultCheckout(true)
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    parameters {
        // the push is optional, so the pipeline also works without Docker Hub credentials
        booleanParam(name: 'PUSH_IMAGE', defaultValue: false,
                     description: 'Push the image to Docker Hub (needs the Jenkins credentials "dockerhub")')
        string(name: 'DOCKERHUB_REPO', defaultValue: 'marin971/hotel-app',
               description: 'Docker Hub repository for the push (user/repository)')
    }

    environment {
        // local image name; the tag is the Jenkins build number, so every build has its own image
        IMAGE_NAME = 'hotel-app'
        IMAGE_TAG  = "${env.BUILD_NUMBER}"
    }

    triggers {
        // check GitHub every 5 minutes and build if there is a new commit
        // (a GitHub webhook cannot reach Jenkins on localhost)
        pollSCM('H/5 * * * *')
    }

    stages {
        stage('Checkout') {
            steps {
                // gets the code from the repository and branch set in the job ("Pipeline script from SCM")
                checkout scm
            }
        }

        stage('Build') {
            agent {
                docker {
                    image 'maven:3.9.6-eclipse-temurin-21'
                    reuseNode true
                }
            }
            steps {
                dir('hotel-app') {
                    // Maven repository inside the workspace, so the jenkins user can write it
                    sh 'mvn -B -Dmaven.repo.local=$WORKSPACE/.m2/repository clean compile'
                }
            }
        }

        stage('Test') {
            agent {
                docker {
                    image 'maven:3.9.6-eclipse-temurin-21'
                    reuseNode true
                }
            }
            steps {
                dir('hotel-app') {
                    sh 'mvn -B -Dmaven.repo.local=$WORKSPACE/.m2/repository test'
                }
            }
            post {
                always {
                    // test results in Jenkins (like the course: junit in post)
                    junit 'hotel-app/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Docker Build') {
            steps {
                script {
                    // docker.build comes from the Docker Pipeline plugin;
                    // it uses hotel-app/Dockerfile (multi-stage: Maven build, then Java 21 runtime)
                    docker.build("${IMAGE_NAME}:${IMAGE_TAG}", 'hotel-app')
                }
                // second name for the same image: the newest build is always hotel-app:latest
                sh 'docker tag $IMAGE_NAME:$IMAGE_TAG $IMAGE_NAME:latest'
                sh 'docker image ls $IMAGE_NAME'
            }
        }

        stage('Docker Push') {
            // only when PUSH_IMAGE is ticked in "Build with Parameters"
            when { expression { params.PUSH_IMAGE } }
            steps {
                // user and access token come from the Jenkins credentials "dockerhub",
                // they are never written in this file and Jenkins hides them in the log
                withCredentials([usernamePassword(credentialsId: 'dockerhub',
                                                  usernameVariable: 'DOCKERHUB_USER',
                                                  passwordVariable: 'DOCKERHUB_TOKEN')]) {
                    sh '''
                        echo "$DOCKERHUB_TOKEN" | docker login -u "$DOCKERHUB_USER" --password-stdin
                        docker tag $IMAGE_NAME:$IMAGE_TAG $DOCKERHUB_REPO:$IMAGE_TAG
                        docker tag $IMAGE_NAME:$IMAGE_TAG $DOCKERHUB_REPO:latest
                        docker push $DOCKERHUB_REPO:$IMAGE_TAG
                        docker push $DOCKERHUB_REPO:latest
                    '''
                }
            }
            post {
                always {
                    sh 'docker logout || true'
                }
            }
        }
    }

    post {
        success { echo 'Pipeline successful: hotel-app built, tested and packaged as a Docker image' }
        failure { echo 'Pipeline failed: check the console output' }
    }
}
