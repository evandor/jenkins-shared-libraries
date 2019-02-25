
def call() {
    env.APPLICATION_NAME =
            input(id: 'env.APPLICATION_NAME', message: 'Welche Anwendung soll deployed werden?', parameters: [
                    [$class     : 'ChoiceParameterDefinition',
                     choices    : ["demo\nwebsite","monitor-server","monicat-backend"],
                     description: 'Welche Instanz soll deployed werden?', name: 'application']
            ])
}
