import io.skysail.jenkins.Instance

def call() {
    env.INSTANCE_NAME =
            input(id: 'env.INSTANCE_NAME', message: 'Welche Instanz soll deployed werden?', parameters: [
                    [$class     : 'ChoiceParameterDefinition',
                     choices    : Instance.getInstanceChoices(),
                     description: 'Welche Instanz soll deployed werden?', name: 'instance']
            ])
}
