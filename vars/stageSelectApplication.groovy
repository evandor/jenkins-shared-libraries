import io.skysail.jenkins.ApplicationRepo

def call(instanceName) {
    env.APPLICATION_KEY =
            input(id: 'env.APPLICATION_KEY', message: 'Was soll deployed werden?', parameters: [
                    [$class     : 'ChoiceParameterDefinition',
                     choices    : ApplicationRepo.getApplicationChoices(instanceName),
                     description: 'Welche Applikation soll deployed werden?', name: 'application']
            ])
}
