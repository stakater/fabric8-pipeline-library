#!/usr/bin/groovy
def call(body) {

    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    def flow = new io.fabric8.Fabric8Commands()

    def token

    withCredentials([usernamePassword(credentialsId: 'cd-github', passwordVariable: 'PASS', usernameVariable: 'USER')]) {
        token = env.PASS
    }

    container(name: 'maven') {

        // update any versions that we want to override
        for ( v in config.pomVersionToUpdate ) {
            flow.searchAndReplaceMavenVersionProperty(v.key, v.value)
        }

        def skipTests = config.skipTests ?: false
        def version = 'PR-' + getNewVersion{} + "-${env.BUILD_NUMBER}"

        stage ('Build + Unit test'){
            // set a unique temp version so we can download artifacts from nexus and run acceptance tests
            sh "./mvnw -U versions:set -DnewVersion=${version}"
            if(config.profile) {
                sh "./mvnw clean -B -e -U deploy -Dmaven.test.skip=${skipTests}"
            } else {
                sh "./mvnw clean -B -e -U deploy -Dmaven.test.skip=${skipTests} -P ${config.profile}"
            }
        }

        stage ('Push snapshot image to registry'){
            retry(3){
                sh "./mvnw fabric8:push -Ddocker.push.registry=${env.FABRIC8_DOCKER_REGISTRY_SERVICE_HOST}:${env.FABRIC8_DOCKER_REGISTRY_SERVICE_PORT}"
            }
        }

        stage('Integration Testing'){
            mavenIntegrationTest {
                environment = 'Test'
                failIfNoTests = false
                itestPattern = '*IT'
            }
        }

        return version
    }
  }
