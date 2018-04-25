#!/usr/bin/groovy

def call(body) {
    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    def args = config.additionalArgs
    def ns = config.environment
    kubeNS = "-Dkubernetes.namespace=${ns} -Dfabric8.use.existing=${ns}"
    echo "Running the integration tests in the namespace: ${ns}"

    if(args) {
      sh "./mvnw org.apache.maven.plugins:maven-failsafe-plugin:2.18.1:integration-test ${kubeNS} -Dit.test=${config.itestPattern} -DfailIfNoTests=${config.failIfNoTests} ${args} org.apache.maven.plugins:maven-failsafe-plugin:2.18.1:verify"
    }
    else {
      sh "./mvnw org.apache.maven.plugins:maven-failsafe-plugin:2.18.1:integration-test ${kubeNS} -Dit.test=${config.itestPattern} -DfailIfNoTests=${config.failIfNoTests} org.apache.maven.plugins:maven-failsafe-plugin:2.18.1:verify"
    }
    junitResults(body);
}
