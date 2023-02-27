//For this pipeline you will need the CloudBees plugin and to make a temp bucket for backups in order to roll back safely
pipeline {
    agent {
        node 'AwsDeployer'
    }
    parameters {
        string(name:'TEAM_NAME', description: 'To which team do you belong too]')
        string(name: 'APP_NAME', description: 'The name of the app you want to deploy]')
        string(name: 'VERSION', description: 'The version you want to deploy')
        string(name: 'BUCKET_NAME', description: 'The name of the S3 Bucket you want to push too')
        string(name: 'TMP_BUCKET', description: 'The name of the temp bucket for rollbacks')
        booleanParam(name: 'SANITY_CHECK', description: 'would you like to do a sanity check?', defaultValue: false)
    }
    environment {
        ARTIFACTORY_ADDRESS="ARTIFACTORY ADRESS"
        ARTIFACTORY_CREDENTIALS=credentials('YOUR CREDENTIALS')
        AWS_CREDENTIALS = credentials('YOUR CREDENTIALS')
    }
    stages{
        stage ('Cleaning Backup Bucket'){
            steps{
                sh "aws s3 rm s3://${TMP_BUCKET} --recursive"
                echo 'temp bucket as been cleaned'
            }
        }
        stage ("create a temp backup"){
            steps {
                sh "aws s3 sync s3://${BUCKET_NAME} s3://${TMP_BUCKET}"
            }
        }
        stage ("Download Artifact") {
            steps {
                dir("work"){
                    sh "curl --insecure ${ARTIFACTORY_ADDRESS}/${params.TEAM_NAME}/${params.APP_NAME}/${params.VERSION}/${params.APP_NAME}.tar.gz --output ${APP_NAME}.tar.gz"
                }            
            } 
        }
        stage ("Push to s3"){
            steps{
                dir("work"){
                    script{
                        sh "pwd"
                        sh "tar -xvzf ${APP_NAME}.tar.gz"
                        sh "aws s3 rm s3://${BUCKET_NAME} --recursive"
                        sh "aws s3 cp . s3://${BUCKET_NAME} --exclude ${APP_NAME}.tar.gz --recursive"
                    }                
                }    
            }
        }
        stage ('Sanity Check'){
            when {
                expression {
                    params.SANITY_CHECK == true
                }
            }
            steps {
                script {
                    continueorrollback = input (
                        id: 'Proceed', message: 'Please Abort if the deployment went smoothly else press Proceed for a rollback',
                        parameters: [
                        [$class: 'BooleanParameterDefinition', defaultValue: false, description: '', name: 'Proceed with deployment?']
                    ])
                    sh "aws s3 rm s3://${BUCKET_NAME} --recursive"
                    sh "aws s3 sync s3://${TMP_BUCKET} s3://${BUCKET_NAME}"
                    sh "aws s3 rm s3://${TMP_BUCKET} --recursive"
                }
            }
        }
    }
    post {
        failure {
            sh "aws s3 rm s3://${BUCKET_NAME} --recursive"
            sh "aws s3 sync s3://${TMP_BUCKET}s3://${BUCKET_NAME}"
            sh "aws s3 rm s3://${TMP_BUCKET} --recursive"
        }
    }
}
