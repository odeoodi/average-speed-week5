pipeline {
    agent any
    tools {
        maven 'Maven3'
    }

    environment {
        PATH = "/usr/local/bin/docker"
        JAVA_HOME = '/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home'  // Adjust to your actual JDK pat
        SONARQUBE_SERVER = 'SonarQubeServer'  // The name of the SonarQube server configured in Jenkins
        SONAR_TOKEN = credentials('SONAR_TOKEN') // Store the token securely
        DOCKERHUB_CREDENTIALS_ID = 'Docker_Hub'
        DOCKERHUB_REPO = 'odeoodi/lectdemo_week5_task'
        DOCKER_IMAGE_TAG = 'latest'
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/odeoodi/average-speed-week5.git'
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean install'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQubeServer') {
                    sh "${tool 'SonarScanner'}/bin/sonar-scanner"
                }
            }
        }



        stage('Build Docker Image') {
            steps {
                script {
                    docker.build("${DOCKERHUB_REPO}:${DOCKER_IMAGE_TAG}")
                    // Or specify Dockerfile path explicitly if needed
                    // docker.build("${DOCKERHUB_REPO}:${DOCKER_IMAGE_TAG}", "-f ./Dockerfile .")
                }
            }
        }

        stage('Push Docker Image to Docker Hub') {
            steps {
                script {
                    docker.withRegistry('https://index.docker.io/v1/', DOCKERHUB_CREDENTIALS_ID) {
                        docker.image("${DOCKERHUB_REPO}:${DOCKER_IMAGE_TAG}").push()
                    }
                }
            }
        }
    }
}
