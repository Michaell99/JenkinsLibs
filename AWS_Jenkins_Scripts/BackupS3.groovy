pipeline {
    agent {
        node 'AwsDeployer'
    }
    parameters {
        string(name:'TEAM_NAME', description: 'To which team do you belong?')
        string(name:'BACKUP_BUCKET', description: 'The name of the backup bucket')
        string(name: 'APP_NAME', description: 'The name of the app you Wish to Backup')
        choice(choices: [
            'Test',
            'Pre',
            'Prod'
        ],
        description: 'The env of the Bucket', name: 'ENV')
        string(name: 'BUCKET_NAME', description: 'The name of the S3 Bucket you need a Backup for')
    }
    environment {
        AWS_CREDENTIALS = credentials('YOUR CREDENTIALS')
    }
    stages{
        stage ("Create Fullbackup") {
            steps{
                sh "aws s3 sync s3://${BUCKET_NAME} s3://${BACKUP_BUCKET}/${TEAM_NAME}/${ENV}/${APP_NAME}/${BUCKET_NAME}"
            }
        }
    }
}
