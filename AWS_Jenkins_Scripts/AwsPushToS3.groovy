pipeline {
    agent {
        node ''
    }
    parameters {
        string(name:'TEAM_NAME', description: '[To which team do you belong too]')
        booleanParam(name: 'PUSH_TO_ANOTHER_BUCKET', defaultValue: false, description: 'would you like to push to another s3 bucket?')
        string(name: 'APP_NAME', description: '[The name of the app you want to deploy]')
        string(name: 'VERSION', description: '[The version you want to deploy')
        string(name: 'BUCKET_NAME', description: '[The name of the S3 Bucket you want to push too]')
        //string(name: 'BUCKET2_NAME', description: '[The name of the second S3 Bucket you want to push too')
    }
    environment {
        //ARTIFACTORY_ADDRESS="REPO/${params.TEAM_NAME}/${params.APP_NAME}/${params.VERSION}"
        ARTIFACTORY_ADDRESS="REPO/${params.REPO_PATH}"
        ARTIFACTORY_CREDENTIALS=credentials('CREDENTIALS')
        GIT_PATH= "NOT DEFINED"
        AWS_CREDENTIALS= "NOT DEFINED"
    }
    stages{
        stage ("Download artifact") {
            steps {
                sh "curl --insecure ${ARTIFACTORY_ADDRESS}/${params.APP_NAME}.tar.gz --output ${APP_NAME}.tar.gz"
            }
        }
        stage ("Push to s3"){
            steps{
                script{
                    sh "pwd"
                    sh "ls -a"
                    sh "aws --version"
                    sh "tar -xvzf ${APP_NAME}.tar.gz"
                    sh "ls -a"
                    sh "aws s3 cp . s3://${BUCKET_NAME} --exclude ${APP_NAME}.tar.gz --recursive"
                }    
            }
        }
    }
}
