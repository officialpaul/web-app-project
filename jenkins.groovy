pipeline {
    agent any

    tools {
        maven "Maven3"
        jdk "Openjdk_17"
    }

   environment {
        DOCKERHUB_CREDENTIALS = credentials('dockerhub-creds')
        DOCKER_IMAGE = 'officialpaul/web-app'
        DOCKER_TAG = 'latest build'
    }

    stages {
        stage ('fetch code') {
            steps {
                script {
                    echo "Pull Source code from Git"
                    git branch: 'master', url: 'https://github.com/paul0581/web-app-project.git'
                }
            }
        }

        stage ('Build App') {
            steps {
                script {
                    echo "Building WAR with Maven"
                    sh 'mvn install -DskipTests'
                }
            }
        }

        stage ('Build Docker Image') {
            steps{
                script {
                    dockerImage = docker.build(awsEcrRegistry + ":$BUILD_NUMBER", "./Docker-files/app/multistage/")
                }
            }
        }

        stage('Docker Login') {
            steps {
                sh """
                  echo "${DOCKERHUB_CREDENTIALS_PSW}" | \
                  docker login -u "${DOCKERHUB_CREDENTIALS_USR}" --password-stdin
                """
            }
        }

        stage('Push Image to Docker Hub') {
            steps {
                sh """
                  docker push ${DOCKER_IMAGE}:${DOCKER_TAG}
                """
            }
        }
    }

    post {
        always {
            node {
                sh 'docker logout'
            }
        }
        failure {
            echo '❌ Build or push failed'
        }
    }
}
 
