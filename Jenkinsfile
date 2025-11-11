// Jenkinsfile for JobBoard - Multi-Module Microservices DevSecOps
pipeline {
    agent any

    tools {
        // Use the default names that Jenkins recognizes
        maven 'maven' 
        jdk 'JAVA_HOME'
    }

    environment {
        // This will automatically fetch the secret text credential we created in Jenkins
        SONAR_AUTH_TOKEN = credentials('sonarqube-token') 
    }

    stages {
        stage('Checkout Code') {
            steps {
                git branch: 'main', url: 'https://github.com/NasriMohamedHedi/JobBoard.git', credentialsId: 'github-token'
            }
        }

        stage('Build & Test All Microservices') {
            steps {
                script {
                    def microservices = ["eureka", "gateway", "candidat", "candidature", "job", "meeting", "notification"]
                    
                    for (service in microservices) {
                        echo "Building and testing microservice: ${service}"
                        dir("backEnd/microservices/${service}") {
                            sh 'mvn clean install'
                        }
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
