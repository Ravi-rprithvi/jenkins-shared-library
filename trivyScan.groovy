def call(String imageName) {
    sh """
        trivy image --exit-code 1 --severity HIGH,CRITICAL ${imageName}
    """
}
