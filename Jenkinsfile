String packageProfiles= 'javax-dependencies';

pipeline {
	
	agent {
		kubernetes {
			yamlFile 'kubernetesPod.yaml'
			defaultContainer 'rpooli-build'
		}
	}
	
	options {
		buildDiscarder(logRotator(numToKeepStr: '10'))
	}
	
	stages {
		
		stage('build + deploy artifacts') {
			steps {
				configFileProvider([configFile(fileId: 'maven-settings-rsb', variable: 'MAVEN_SETTINGS_RSB')]) {
					sh "mvn clean package deploy\
							-P ${packageProfiles},it -U\
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
			archiveArtifacts(
					artifacts: '**/target/dependency-*.txt',
					fingerprint: true )
			
			junit '**/target/failsafe-reports/*.xml'
		}
	}
	
}
