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
                container('rpooli-build') {
                    configFileProvider([configFile(fileId: 'maven-settings-rsb', variable: 'MAVEN_SETTINGS_RSB')]) {
                        sh 'mvn clean verify -Pjavax-dependencies,it'
                        sh 'mvn -s $MAVEN_SETTINGS_RSB deploy -Pjavax-dependencies'
                    }
                }
            }
        }
    }
	
	post {
		always {
			junit '**/target/failsafe-reports/*.xml'
		}
	}
	
}
