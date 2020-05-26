pipeline {

    agent {
        kubernetes {
            yamlFile 'kubernetesPod.yaml'
        }
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '3'))
    }
    
    stages {
        
        stage('build and deploy to nexus'){
        
            steps {
            
                container('maven') {
                    sh 'mvn -P-javax-dependencies -U clean package'
                }
                
                container('rpooli-tests'){
                    sh 'mvn verify -Pit'
                }
                
                container('maven') {
                    configFileProvider([configFile(fileId: 'maven-settings-rsb', variable: 'MAVEN_SETTINGS_RSB')]) {
                         sh 'mvn -s $MAVEN_SETTINGS_RSB deploy'
                    }
                }
                
            }

        }
    }
}
