#!/usr/bin/groovy
package io.stakater.charts


def upload(String location, String chartName, String fileName, String cmUrl) {
    sh """
        cd ${location}/${chartName}
        curl -L --data-binary \"@${fileName}\" ${cmUrl}
    """
}

def upload(String location, String chartName, String fileName) {
    upload(location, chartName, fileName, "http://chartmuseum/api/charts")
}

return this