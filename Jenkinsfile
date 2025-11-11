//// Jenkinsfile for JobBoard - Multi-Module Microservices DevSecOps
pipeline {
    agent any


    options {
        // This prevents Jenkins from doing its own default checkout
        skipDefaultCheckout(true) 
    }

    tools {
        maven 'maven' 
        jdk 'JAVA_HOME'
    }

    environment {
        SONAR_AUTH_TOKEN = credentials('sonarqube-token') 
    }

    stages {
        // REMOVE THIS ENTIRE STAGE - IT IS REDUNDANT
        /*
        stage('Checkout Code') {
            steps {
                git branch: 'main', url: 'https://github.com/NasriMohamedHedi/JobBoard.git', credentialsId: 'github-token'
            }
        }
        */

        stage('Build & Test All Microservices') {
            steps {
                // We must explicitly check out the code here
                git branch: 'main', url: 'https://github.com/NasriMohamedHedi/JobBoard.git', credentialsId: 'github-token'
                
                script {
                    def microservices = ["eureka", "gateway", "candidat", "candidature", "job", "meeting", "notification"]
                    
                    for (service in microservices) {
                        echo "Building and testing microservice: ${service}"
                        // Now that we are sure the code is checked out, this should work
                        sh "cd backEnd/microservices/${service} && mvn clean install"
                    }
                }
            }
            post {
                always {
                    junit 'backEnd/**/target/surefire-reports/*.xml'
                }
            }
        }



        stage('Secrets Scan (Gitleaks)') {
            steps {
                echo "Scanning entire repository for exposed secrets with Gitleaks..."
                sh 'gitleaks detect --source . --verbose --report-path gitleaks-report.json'
                archiveArtifacts artifacts: 'gitleaks-report.json', fingerprint: true
            }
        }

        stage('SAST (SonarQube)') {
            steps {
                echo "Scanning backEnd microservices with SonarQube..."
                dir('backEnd') {
                    withSonarQubeEnv('SonarQube') {
                        sh 'mvn sonar:sonar -Dsonar.login=$SONAR_AUTH_TOKEN'
                    }
                }
            }
        }

        stage('SCA (Dependency Check)') {
            steps {
                echo "Scanning backEnd for vulnerable dependencies with Trivy..."
                sh 'trivy fs --format json --output trivy-report.json backEnd/'
                archiveArtifacts artifacts: 'trivy-report.json', fingerprint: true
            }
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 10, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }
    }
    
    post {
        always {
            echo 'Pipeline finished. Cleaning workspace.'
            cleanWs()
        }
        success {
            echo 'Pipeline SUCCEEDED!'
        }
        failure {
            echo 'Pipeline FAILED!'
        }
    }
}
