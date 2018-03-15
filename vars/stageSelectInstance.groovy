import io.skysail.jenkins.Instance

def call() {
    env.INSTANCE_NAME =
            input(id: 'env.INSTANCE_NAME', message: 'Welche Anwendung soll deployed werden?', parameters: [
                    [$class     : 'ChoiceParameterDefinition',
                     choices    : ["demo", "website"],
                     description: 'Welche Instanz soll deployed werden?', name: 'instance']
            ])
}
