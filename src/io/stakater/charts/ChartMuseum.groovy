#!/usr/bin/groovy
package io.stakater.charts


def upload(String location, String chartName, String fileName, String cmUrl) {
    sh """
        cd ${location}/${chartName}
        exitCode=\$(curl -L -s -o /dev/null --data-binary --write-out "%{http_code}" \"@${fileName}\" ${cmUrl});
        if [ \$exitCode -lt 200 ] || [ \$exitCode -gt 299 ]
        then
            echo "Could not Upload Chart"
            exit 1
        fi
    """
}

def upload(String location, String chartName, String fileName, String chartmuseumApiUrl) {
    upload(location, chartName, fileName, chartmuseumApiUrl)
}

def upload(String location, String chartName, String fileName, String cmUrl, String cmUsername, String cmPassword) {
    sh """
        cd ${location}/${chartName}
        exitCode=\$(curl --user ${cmUsername}:${cmPassword} -s -o /dev/null --write-out "%{http_code}" -L --data-binary \"@${fileName}\" ${cmUrl});
        if [ \$exitCode -lt 200 ] || [ \$exitCode -gt 299 ]
        then
            echo "Could not Upload Chart"
            exit 1
        fi
    """
}

def upload(String location, String chartName, String fileName, String cmUsername, String cmPassword, String chartmuseumApiUrl) {
    upload(location, chartName, fileName, chartmuseumApiUrl, cmUsername, cmPassword)
}

return this
