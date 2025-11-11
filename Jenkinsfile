// Jenkinsfile for JobBoard - Full DevSecOps
pipeline {
    agent any

    tools {
    // Use the generic names that Jenkins recognizes by default
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
                // This will pull from YOUR forked repository
                git branch: 'main', url: 'https://github.com/NasriMohamedHedi/JobBoard.git', credentialsId: 'github-token'
            }
        }

        stage('Build & Test') {
            steps {
                // 'mvn verify' compiles code, runs tests, and packages the JAR
                sh 'mvn clean verify' 
            }
            post {
                always {
                    // Publishes the test results for viewing in Jenkins
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        // --- SECURITY STAGES ---

        stage('Secrets Scan (Gitleaks)') {
            steps {
                echo "Scanning for exposed secrets with Gitleaks..."
                sh 'gitleaks detect --source . --verbose --report-path gitleaks-report.json'
                // Save the report as an artifact of the build
                archiveArtifacts artifacts: 'gitleaks-report.json', fingerprint: true
            }
        }

        stage('SAST (SonarQube)') {
            steps {
                script {
                    // Run the SonarQube scan
                    withSonarQubeEnv('SonarQube') {
                        sh 'mvn sonar:sonar -Dsonar.login=$SONAR_AUTH_TOKEN'
                    }
                }
            }
        }

        stage('SCA (Dependency Check)') {
            steps {
                echo "Scanning for vulnerable dependencies with Trivy..."
                // Trivy scans the filesystem for known vulnerabilities in dependencies
                sh 'trivy fs --format json --output trivy-report.json .'
                archiveArtifacts artifacts: 'trivy-report.json', fingerprint: true
            }
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    // This step pauses the pipeline and waits for SonarQube to finish analysis
                    // It will then check the Quality Gate status and fail the build if needed.
                    waitForQualityGate abortPipeline: true
                }
            }
        }
    }
    
    post {
        always {
            echo 'Pipeline finished. Cleaning workspace.'
            cleanWs() // Clean the workspace for the next build
        }
        success {
            echo 'Pipeline SUCCEEDED!'
        }
        failure {
            echo 'Pipeline FAILED!'
        }
    }
}
