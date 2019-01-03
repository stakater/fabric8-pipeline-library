#!/usr/bin/groovy
package io.stakater.charts

def uploadToChartMuseum(String location, String chartName, String fileName, String chartmuseumApiUrl) {
    def chartMuseum = new io.stakater.charts.ChartMuseum()
    chartMuseum.upload(location, chartName, fileName, chartmuseumApiUrl)
}

def uploadToChartMuseum(String location, String chartName, String fileName, String cmUsername, String cmPassword, String chartmuseumApiUrl) {
    def chartMuseum = new io.stakater.charts.ChartMuseum()
    chartMuseum.upload(location, chartName, fileName, cmUsername, cmPassword, chartmuseumApiUrl)
}

def uploadToStakaterCharts(String packagedChart) {
    def git = new io.stakater.vc.Git()

    def chartRepoName = "stakater-charts"
    def chartRepoUrl = "git@github.com:stakater/${chartRepoName}.git"
    
    git.checkoutRepo(chartRepoUrl, "master", chartRepoName)
    sh """
        mv ${packagedChart} ${chartRepoName}/docs
        cd ${chartRepoName}
        helm repo index docs --url https://stakater.github.io/${chartRepoName}
    """

    git.commitChanges(chartRepoName, "Update charts")
}

return this
