pipeline {
    agent any

    tools {
        maven "Maven3"
        jdk "Openjdk_17"
    }

    environment {
        DOCKERHUB_CREDENTIALS = credentials('dockerhub-creds')
        DOCKER_IMAGE = 'officialpaul/web-app'
        DOCKER_TAG = 'latest-build'
    }

    stages {

        stage('Fetch Code') {
            steps {
                echo "Pull Source code from Git"
                git branch: 'master', url: 'https://github.com/paul0581/web-app-project.git'
            }
        }

        stage('Build App') {
            steps {
                echo "Building WAR with Maven"
                sh 'mvn install -DskipTests'
            }
        }

        stage('Compile') {
            steps {
                sh 'mvn -Dmaven.test.failure.ignore=true clean compile'
            }
        }

        stage('Docker Login') {
            steps {
                sh '''
                    echo "$DOCKERHUB_CREDENTIALS_PSW" | \
                    docker login -u "$DOCKERHUB_CREDENTIALS_USR" --password-stdin
                '''
            }
        }

        stage('Push Image to Docker Hub') {
            steps {
                sh '''
                    docker push ${DOCKER_IMAGE}:${DOCKER_TAG}
                '''
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
