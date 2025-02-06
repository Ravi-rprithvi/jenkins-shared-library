@Library('jenkins-shared-library') _
pipeline {
    agent any

    parameters {
        choice(name: 'DEPLOY_ENV', choices: ['dev', 'qa', 'prod'], description: 'Select the deployment environment')
        string(name: 'SONARQUBE_HOST', defaultValue: 'http://sonarqube.company.com', description: 'SonarQube server URL')
        string(name: 'SONARQUBE_PROJECT_KEY', defaultValue: 'my-nodejs-project', description: 'SonarQube project key')
    }

    environment {
        REGISTRY = "123456789012.dkr.ecr.us-east-1.amazonaws.com/my-node-app"
        EKS_CLUSTER = "my-eks-cluster"
        AWS_REGION = "us-east-1"
        NAMESPACE = "${params.DEPLOY_ENV}"
        IMAGE_TAG = "${params.DEPLOY_ENV}-${BUILD_NUMBER}"
        IMAGE_NAME = "${REGISTRY}:${IMAGE_TAG}"
        KUBE_MANIFEST = "k8s/deployment-${params.DEPLOY_ENV}.yaml"
    }

    stages {
        stage('Checkout Code') {
            steps {
                gitCheckout('https://github.com/my-org/my-node-app.git', 'main')
            }
        }

        stage('Install Dependencies') {
            steps {
                npmInstall()
            }
        }

        stage('SonarQube Scan') {
            steps {
                withCredentials([string(credentialsId: 'sonar-token', variable: 'SONARQUBE_TOKEN')]) {
                    withSonarQubeEnv('SonarQube') {
                        sh "sonar-scanner -Dsonar.projectKey=${params.SONARQUBE_PROJECT_KEY} -Dsonar.host.url=${params.SONARQUBE_HOST} -Dsonar.login=$SONARQUBE_TOKEN"
                    }
                }
            }
        }

        stage('Docker Image Build and Push') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'docker-registry-credentials', usernameVariable: 'DOCKER_USERNAME', passwordVariable: 'DOCKER_PASSWORD')]) {
                    sh """
                        docker login -u $DOCKER_USERNAME -p $DOCKER_PASSWORD $REGISTRY
                        docker build -t $REGISTRY/my-node-app:$IMAGE_TAG .
                        docker push $REGISTRY/my-node-app:$IMAGE_TAG
                    """
                }
            }
        }

        stage('Trivy Docker Image Scan') {
            steps {
                trivyScan("${REGISTRY}/my-node-app:${IMAGE_TAG}")
            }
        }

        stage('Deploy to Kubernetes (EKS)') {
            steps {
                withCredentials([kubeconfigFile(credentialsId: 'k8s-credentials', variable: 'KUBECONFIG')]) {
                    sh """
                        kubectl apply -f ${KUBE_MANIFEST} --namespace=${NAMESPACE}
                        kubectl rollout status deployment -n ${NAMESPACE}
                    """
                }
            }
        }
    }

    post {
        always {
            // Clean up, notify, etc.
        }

        success {
            echo "Pipeline executed successfully!"
        }

        failure {
            echo "Pipeline failed!"
        }
    }
}
