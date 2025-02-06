def call(String imageName, String tag) {
    sh """
        docker build -t ${imageName}:${tag} .
        docker push ${imageName}:${tag}
    """
}
