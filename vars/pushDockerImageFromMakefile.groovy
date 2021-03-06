#!/usr/bin/groovy

def call(body) {
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    def dockerRepositoryURL = config.dockerRepositoryURL ?: common.getEnvValue('DOCKER_REPOSITORY_URL')

    toolsNode(toolsImage: 'stakater/pipeline-tools:1.5.2') {
        def common = new io.stakater.Common()
        def docker = new io.stakater.containers.Docker()
        def git = new io.stakater.vc.Git()
        def slack = new io.stakater.notifications.Slack()

        // Slack variables
        def slackChannel = "${env.SLACK_CHANNEL}"
        def slackWebHookURL = "${env.SLACK_WEBHOOK_URL}"

        container(name: 'tools') {
            withCurrentRepo { def repoUrl, def repoName, def repoOwner, def repoBranch ->         
                def imageName = repoName.split("dockerfile-").last().toLowerCase()                       
                def dockerImage = "${dockerRepositoryURL}/${repoOwner.toLowerCase()}/${imageName}"                
                def dockerImageVersion
                try {
                    stage('Canary Release') {                        
                        docker.buildAndPushImageFromMakefile(dockerRepositoryURL,repoOwner,imageName)
                        dockerImageVersion = common.shOutput """
                            release=\$(cat .release)
                            pattern="release="
                            ImageVersion=\${release/\$pattern/}
                            echo \$ImageVersion
                        """
                    }
                }
                catch (e) {
                    slack.sendDefaultFailureNotification(slackWebHookURL, slackChannel, [slack.createErrorField(e)])
                
                    def commentMessage = "Yikes! You better fix it before anyone else finds out! [Build ${env.BUILD_NUMBER}](${env.BUILD_URL}) has Failed!"
                    git.addCommentToPullRequest(commentMessage)

                    throw e
                }

                stage('Notify') {
                    slack.sendDefaultSuccessNotification(slackWebHookURL, slackChannel, [slack.createDockerImageField("${dockerImage}:${dockerImageVersion}")])

                    def commentMessage = "Image is available for testing. `docker pull ${dockerImage}:${dockerImageVersion}`"
                    git.addCommentToPullRequest(commentMessage)
                }
            }
        }
    }
}
