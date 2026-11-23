// Jenkins pipeline for hotel-app (Assignment 5)
// Based on the course (XII. Jenkins, Example 9-1). Differences:
// - Maven runs in a Docker container with Java 21 (course uses Java 17, our app needs 21)
// - the app is in the subfolder hotel-app, so Maven runs there
pipeline {
    agent any

    options {
        // we do the checkout ourselves in the stage "Checkout", so it shows up in the Stage View
        skipDefaultCheckout(true)
        buildDiscarder(logRotator(numToKeepStr: '10'))
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
    }

    post {
        success { echo 'Pipeline successful: hotel-app built and tested' }
        failure { echo 'Pipeline failed: check the console output' }
    }
}
