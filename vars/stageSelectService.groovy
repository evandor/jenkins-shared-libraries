
def call() {
    env.APPLICATION_NAME =
            input(id: 'env.APPLICATION_NAME', message: 'Welche Anwendung soll deployed werden?', parameters: [
                    [$class     : 'ChoiceParameterDefinition',
                     choices    : "skysail-service-monitor\n" +
                             "rse\nrse-web-client\n" +
                             "notes\nnotes-web-client",
                     description: 'Welche Instanz soll deployed werden?', name: 'application']
            ])
}
