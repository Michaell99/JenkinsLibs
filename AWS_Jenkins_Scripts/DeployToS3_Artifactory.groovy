//For this pipeline you will need the CloudBees plugin and to make a temp bucket for backups in order to roll back safely
pipeline {
    agent {
        node 'AwsDeployer'
    }
    parameters {
        string(name:'TEAM_NAME', description: '[To which team do you belong too]')
        string(name: 'APP_NAME', description: '[The name of the app you want to deploy]')
        string(name: 'VERSION', description: '[The version you want to deploy')
        string(name: 'BUCKET_NAME', description: '[The name of the S3 Bucket you want to push too]')
        booleanParam(name: 'SANITY_CHECK', defaultValue: false, description: 'would you like to do a sanity check?')
    }
    environment {
        ARTIFACTORY_ADDRESS="ARTIFACTORY ADRESS"
        ARTIFACTORY_CREDENTIALS=credentials('YOUR CREDENTIALS')
        AWS_CREDENTIALS = credentials('YOUR CREDENTIALS')
    }

    stages{
        stage ('Cleaning Backup Buckets'){
            steps{
                sh "echo ***************************************************************************************************************"
                sh "echo ***************************************Starting to cleann Temp Backup*****************************************************"
                sh "aws s3 rm s3://YOUR TEMP S3 BACKUP BUCKET --recursive"
                sh "echo ***************************************************************************************************************"
                sh "echo ***************************************Temp Backup Has Been Cleaned****************************************************"

            }
        }
        stage ("create a temp backup"){
            steps {
                sh "echo ***************************************************************************************************************"
                sh "echo ***************************************TEMP BACKUP STARTED**********************************************************"
                sh "aws s3 sync s3://${BUCKET_NAME} s3://YOUR TEMP S3 BACKUP BUCKET"
                sh "echo ***************************************************************************************************************"
                sh "echo ***************************************TEMP BACKUP FINISHED*********************************************************"
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
                        sh "echo ***************************************************************************************************************"
                        sh "echo ***************************************DEPLOYMENT STARTED******************************************************"
                        sh "aws s3 rm s3://${BUCKET_NAME} --recursive"
                        sh "aws s3 cp . s3://${BUCKET_NAME} --exclude ${APP_NAME}.tar.gz --recursive"
                        sh "echo ***************************************************************************************************************"
                        sh "echo ***************************************DEPLOYMENT FINISHED*****************************************************"
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
                    sh "echo ***************************************************************************************************************"
                    sh "echo ***************************************ROLLBACK STARTED******************************************************"
                    sh "aws s3 rm s3://${BUCKET_NAME} --recursive"
                    sh "aws s3 sync s3://YOUR TEMP S3 BACKUP BUCKET s3://${BUCKET_NAME}"
                    sh "aws s3 rm s3://YOUR TEMP S3 BACKUP BUCKET --recursive"
                    sh "echo ***************************************************************************************************************"
                    sh "echo ***************************************ROLLBACK FINISHED*******************************************************"
                    sh "echo *******************************This Pipeline was made by Michael***********************************************"
                }
            }
        }
    }
}
