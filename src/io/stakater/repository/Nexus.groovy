#!/usr/bin/groovy
package io.stakater.repository

def pushAppArtifact(String appName, String version, String nexusUrl) {
  def repositoryType = version.contains("SNAPSHOT") ? "snapshots" : "releases"
  sh """
    mvn deploy:deploy-file -DgeneratePom=false -DrepositoryId=nexus -Durl=${nexusUrl}/repository/maven-${repositoryType} -DpomFile=pom.xml -Dfile=target/${appName}-${version}.jar
  """
}

return this