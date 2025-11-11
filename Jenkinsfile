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
                // We use a script block to allow for more complex logic, like loops
                script {
                    // Define the list of microservices to build
                    def microservices = ["eureka", "gateway", "candidat", "candidature", "job", "meeting", "notification"]
                    
                    // Loop through each service and build it
                    for (service in microservices) {
                        echo "Building and testing microservice: ${service}"
                        dir("backEnd/microservices/${service}") {
                            // 'mvn install' will compile, test, and package the JAR
                            // It also installs the artifact into the local Maven repository, which might be needed by other services
                            sh 'mvn clean install'
                        }
                    }
                }
            }
            post {
                always {
                    // Collect test results from ALL microservices
                    // The '**' is a wildcard that searches subdirectories
                    junit 'backEnd/**/target/surefire-reports/*.xml'
                }
            }
        }

        // --- SECURITY STAGES ---

        stage('Secrets Scan (Gitleaks)') {
            steps {
                echo "Scanning entire repository for exposed secrets with Gitleaks..."
                // We scan the root of the workspace ('.')
                sh 'gitleaks detect --source . --verbose --report-path gitleaks-report.json'
                archiveArtifacts artifacts: 'gitleaks-report.json', fingerprint: true
            }
        }

        stage('SAST (SonarQube)') {
            steps {
                echo "Scanning backEnd microservices with SonarQube..."
                // We run the scan from the root of the backEnd directory
                // SonarQube is smart enough to find all the pom.xml files and analyze them as a multi-module project
                dir('backEnd') {
                    // We need to tell SonarQube where the parent pom is, if there is one.
                    // In this case, we'll just run the scan on all subdirectories.
                    withSonarQubeEnv('SonarQube') {
                        sh 'mvn sonar:sonar -Dsonar.login=$SONAR_AUTH_TOKEN'
                    }
                }
            }
        }

        stage('SCA (Dependency Check)') {
            steps {
                echo "Scanning backEnd for vulnerable dependencies with Trivy..."
                // Scan the entire backEnd directory for all dependencies
                sh 'trivy fs --format json --output trivy-report.json backEnd/'
                archiveArtifacts artifacts: 'trivy-report.json', fingerprint: true
            }
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 10, unit: 'MINUTES') { // Increased timeout for a larger project
                    // This will fail the pipeline if the Quality Gate fails in SonarQube
                    waitForQualityGate abortPipeline: true
                }
            }
        }
    }
    
       post {
        always {
            // This is the CORRECT place for cleanWs()
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
}
