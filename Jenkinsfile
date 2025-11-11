// Jenkinsfile for JobBoard - Multi-Module Microservices DevSecOps
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
        stage('Build & Test All Microservices') {
            steps {
                // First, checkout the code
                git branch: 'main', url: 'https://github.com/NasriMohamedHedi/JobBoard.git', credentialsId: 'github-token'

                // The 'for' loop must be inside a 'script' block
                script {
                    def microservices = ["eureka", "gateway", "candidat", "candidature", "job", "meeting", "notification"]
                    
                    for (service in microservices) {
                        echo "Building and testing microservice: ${service}"
                        // Use a full path with shell command to be 100% explicit
                        sh """
                        cd "${WORKSPACE}/backEnd/microservices/${service}"
                        mvn clean install
                        """
                    }
                }
            }
            // The 'post' block for junit must be inside the 'stage' block
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
    
    // This is the main 'post' block that runs at the end of the entire pipeline
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
