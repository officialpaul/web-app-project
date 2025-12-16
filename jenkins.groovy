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

        stage('Build') {
            steps {
                // Get some code from a GitHub repository 
                git 'https://github.com/officialpaul/web-app-project.git'
                sh "mvn -Dmaven.test.failure.ignore=true clean compile"
            }
        }


        stage('Docker Login') {
            steps {
                sh """
                  echo "${dockerhub-creds}" | \
                  docker login -u "${dockerhub-creds}" --password-stdin
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
        success {
            echo '✅ Build and push completed successfully'
        }
        failure {
            echo '❌ Build or push failed'
        }
    }
}
 
