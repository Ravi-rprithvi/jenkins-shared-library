def call(String kubeManifest, String namespace) {
    sh """
        kubectl apply -f ${kubeManifest} --namespace=${namespace}
    """
}
