String packageProfiles= 'javax-dependencies';

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
						sh "mvn clean verify \
								-P ${packageProfiles},it -U \
								--batch-mode"
						sh "mvn deploy \
								-P ${packageProfiles} \
								--batch-mode -s $MAVEN_SETTINGS_RSB"
						sh "mvn -f webapp/ site-deploy \
								-P ${packageProfiles} \
								--batch-mode -s $MAVEN_SETTINGS_RSB"
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
