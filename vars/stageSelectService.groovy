
def call() {
    env.APPLICATION_NAME =
            input(id: 'env.APPLICATION_NAME', message: 'Welche Anwendung soll deployed werden?', parameters: [
                    [$class     : 'ChoiceParameterDefinition',
                     choices    : "portal\nnotes-service\nbookmarks-service\nskysail-service-monitor\n" +
                             "rse\nrse-web-client\n" +
                             "notes\nnotes-web-client\n" +
                             "skysail-service-boot-admin",
                     description: 'Welche Instanz soll deployed werden?', name: 'application']
            ])
}
