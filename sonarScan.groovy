def call(String sonarHost, String sonarProjectKey) {
    withSonarQubeEnv('SonarQube') {
        sh "sonar-scanner -Dsonar.projectKey=${sonarProjectKey} -Dsonar.host.url=${sonarHost}"
    }
}
