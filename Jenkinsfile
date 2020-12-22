String packageProfiles= 'javax-dependencies';

pipeline {
	
	agent {
		kubernetes {
			yamlFile 'kubernetesPod.yaml'
			defaultContainer 'rpooli-build'
		}
	}
	
	options {
		buildDiscarder(logRotator(numToKeepStr: '3'))
	}
	
	stages {
		
		stage('build + deploy artifacts') {
			steps {
				configFileProvider([configFile(fileId: 'maven-settings-rsb', variable: 'MAVEN_SETTINGS_RSB')]) {
					sh "mvn clean verify\
							-P ${packageProfiles},it -U\
							--batch-mode\
							-Dmaven.test.failure.ignore=true"
					sh "mvn deploy\
							-P ${packageProfiles}\
							--batch-mode -s $MAVEN_SETTINGS_RSB\
							-Dmaven.test.failure.ignore=true"
				}
			}
		}
		
		stage('generate + deploy site') {
			steps {
				configFileProvider([configFile(fileId: 'maven-settings-rsb', variable: 'MAVEN_SETTINGS_RSB')]) {
					sh "mvn -f webapp/ site-deploy\
							-P ${packageProfiles}\
							--batch-mode -s $MAVEN_SETTINGS_RSB"
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
