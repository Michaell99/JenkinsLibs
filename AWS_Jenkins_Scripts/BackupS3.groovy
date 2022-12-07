//You will need the cloudbees plugin for aws credentials for this pipeline.
//You will also need to make a general bucket for backups this pipeline will help you out and make the right folders so many teams can use the same bucket
pipeline {
    agent {
        node 'AwsDeployer'
    }
    parameters {
        string(name:'TEAM_NAME', description: '[To which team do you belong too]')
        string(name: 'APP_NAME', description: '[The name of the app you Wish to Backup]')
        choice(choices: [
            'Test',
            'Pre',
            'Prod'
        ],
        description: 'The env of the Bucket', name: 'ENV')
        string(name: 'BUCKET_NAME', description: '[The name of the S3 Bucket you need a Backup for]')

    }
    environment {
        ARTIFACTORY_ADDRESS="NOT DEFINED"
        ARTIFACTORY_CREDENTIALS= "NOT DEFINED"
        AWS_CREDENTIALS = "Put Your Credentials Here Use The ClouadBees plugin for AWS Credentials"
    }

    stages{
        stage ("Create Fullbackup") {
            steps{
                sh "echo ***************************************************************************************************************"
                sh "echo ***************************************BACKUP STARTED**********************************************************"
                sh "aws s3 sync s3://${BUCKET_NAME} s3://YOUR GENERAL BACKUP BUCKET/${TEAM_NAME}/${ENV}/${APP_NAME}/${BUCKET_NAME}"
                sh "echo ***************************************************************************************************************"
                sh "echo ***************************************BACKUP FINISHED*********************************************************"
                sh "echo *******************************This Pipeline was made by Michael***********************************************"
            }
        }
    }
}
