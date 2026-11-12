// DineDash CI/CD
//  - every branch: unit tests + frontend build
//  - main: build images for changed components, push to ECR, roll out on ECS
pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '20'))
        timeout(time: 45, unit: 'MINUTES')
    }

    environment {
        AWS_REGION    = 'ap-south-1'
        ECS_CLUSTER   = 'dinedash'
        // Account id comes from Jenkins credentials so it never sits in the repo
        AWS_ACCOUNT_ID = credentials('aws-account-id')
        ECR_REGISTRY  = "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"
        IMAGE_TAG     = "${env.GIT_COMMIT?.take(8) ?: 'manual'}"
    }

    stages {
        stage('Detect changes') {
            steps {
                script {
                    def base = env.GIT_PREVIOUS_SUCCESSFUL_COMMIT ?: 'HEAD~1'
                    def out = sh(script: "ci/changed-services.sh ${base}", returnStdout: true).trim()
                    env.CHANGED = out.replaceAll('\n', ' ')
                    echo "Changed components: ${env.CHANGED ?: 'none'}"
                }
            }
        }

        stage('Backend tests') {
            steps {
                sh './mvnw -B -ntp verify'
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Frontend build') {
            when { expression { env.CHANGED.contains('frontend') } }
            agent {
                docker {
                    image 'node:22-alpine'
                    reuseNode true
                }
            }
            steps {
                dir('frontend') {
                    sh 'npm ci && npm run lint && npm run build'
                }
            }
        }

        stage('Build & push images') {
            when {
                branch 'main'
                expression { env.CHANGED?.trim() }
            }
            steps {
                withAWS(region: env.AWS_REGION, credentials: 'aws-jenkins') {
                    sh 'aws ecr get-login-password | docker login --username AWS --password-stdin "$ECR_REGISTRY"'
                    script {
                        env.CHANGED.split(' ').findAll { it }.each { component ->
                            sh "ci/build-and-push.sh ${component} ${ECR_REGISTRY} ${IMAGE_TAG}"
                        }
                    }
                }
            }
        }

        stage('Deploy to ECS') {
            when {
                branch 'main'
                expression { env.CHANGED?.trim() }
            }
            steps {
                withAWS(region: env.AWS_REGION, credentials: 'aws-jenkins') {
                    script {
                        env.CHANGED.split(' ').findAll { it }.each { component ->
                            sh "infra/aws/scripts/deploy-ecs.sh ${component} ${ECR_REGISTRY}/dinedash/${component}:${IMAGE_TAG}"
                        }
                    }
                }
            }
        }
    }

    post {
        always {
            sh 'docker image prune -f || true'
        }
        failure {
            echo "Build failed for ${env.BRANCH_NAME} @ ${env.IMAGE_TAG}"
        }
    }
}
