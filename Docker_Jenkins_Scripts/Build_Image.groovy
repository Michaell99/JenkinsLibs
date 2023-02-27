pipeline {
    agent {
        node "Dockerbuilder"
    }
    parameters {
        string(name: 'ORGANIZATION_UNIT', description: 'organization unit')
        string(name: 'TEAM_NAME', description: 'The team you belone to')
        string(name: 'IMAGE_NAME', description: 'The name of the image')
        string(name: 'TAR_NAME', description: 'The name of the Artifact tar')
        string(name: 'ARTIFACT_URI', description: 'Please paste the path to pull the artifact from')
        string(name: 'VERSION', description: 'The version of the image')
    }
    environment {
        ARTIFACTORY_CREDENTIALS = credentials('YOUR CREDENTIALS')
        TargetRepo = "https://example/${params.ORGANIZATION_UNIT}/${params.TEAM_NAME}/${params.IMAGE_NAME}:${params.VERSION}"
    }
    stages {
        stage ('Fetching artifacts'){
            steps {
                sh "curl --insecure ${ARTIFACT_URI} --output ${TAR_NAME}"
                sh "tar -xvf ${TAR_NAME}"
            }
        }
        stage ('Build Image') {
            steps {
                sh "docker build . -t ${env.TargetRepo}"
            }
        }
        stage ('Push Image') {
            steps {
                sh "docker push ${env.TargetRepo}"
            }
        }
    }
}