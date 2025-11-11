pipeline {
    agent any

    stages {
        stage('Build & Test All Microservices') {
            steps {
                // Checkout the code
                git branch: 'main', url: 'https://github.com/NasriMohamedHedi/JobBoard.git', credentialsId: 'github-token'
                
                // Debugging: List all files in the workspace
                echo '--- DEBUGGING: Listing all files in the workspace ---'
                sh 'ls -lR ${WORKSPACE}'
                echo '--- END DEBUGGING ---'
                
                script {
                    def services = [
                        [name: "eureka", path: "backEnd/eureka"],
                        [name: "gateway", path: "backEnd/gateway"],
                        [name: "candidat", path: "backEnd/microservices/candidat"],
                        [name: "candidature", path: "backEnd/microservices/candidature"],
                        [name: "job", path: "backEnd/microservices/job"],
                        [name: "meeting", path: "backEnd/microservices/meeting"],
                        [name: "notification", path: "backEnd/microservices/notification"]
                    ]
                    
                    for (service in services) {
                        echo "Building and testing microservice: ${service.name}"
                        sh """
                        cd "${WORKSPACE}/${service.path}"
                        pwd
                        mvn clean install
                        """
                    }
                }
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }
        
        stage('Secrets Scan (Gitleaks)') {
            steps {
                sh 'gitleaks detect --source=. -v || exit 1'  // Fail on secrets found
            }
        }
        
        stage('SAST (SonarQube)') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh 'mvn sonar:sonar -Dsonar.projectKey=JobBoard -Dsonar.host.url=http://your-sonarqube-url'
                }
            }
        }
        
        stage('SCA (Dependency Check)') {
            steps {
                dependencyCheck additionalArguments: '--scan . --format ALL', odcInstallation: 'Dependency-Check'
                dependencyCheckPublisher pattern: '**/dependency-check-report.xml'
            }
        }
        
        stage('Docker Scan') {
            steps {
                // Assuming a Docker build step is added; adjust as needed
                // First, build Docker images if not already (example for a service)
                sh 'docker build -t your-image:tag backEnd/microservices/job'  // Example; repeat for others
                sh 'trivy image --exit-code 1 --no-progress your-image:tag'
            }
        }
        
        stage('Quality Gate') {
            steps {
                timeout(time: 1, unit: 'HOURS') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }
        
        stage('DAST (OWASP ZAP)') {
            steps {
                // Assuming deployment to staging; add deploy steps if needed
                sh 'zap-baseline.py -t http://staging-url -r zap-report.html || exit 1'
            }
            post {
                always {
                    publishHTML(target: [reportName: 'ZAP Scan Report', reportDir: '.', reportFiles: 'zap-report.html'])
                }
            }
        }
    }
    
    post {
        always {
            echo 'Pipeline finished. Cleaning workspace.'
            cleanWs()
        }
        failure {
            echo 'Pipeline FAILED!'
            // Add notifications, e.g., Slack or Email
            slackSend channel: '#devops', color: 'danger', message: "Build failed: ${env.BUILD_URL}"
        }
        success {
            slackSend channel: '#devops', color: 'good', message: "Build succeeded: ${env.BUILD_URL}"
        }
    }
}
