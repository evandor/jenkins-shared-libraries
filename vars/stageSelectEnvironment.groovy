
def call() {
    env.STAGE =
            input(id: 'env.STAGE', message: 'Which stage should be deployed to?', parameters: [
                    [$class     : 'ChoiceParameterDefinition',
                     choices    : "test\n" +
                             "int\n" +
                             "prod",
                     description: 'which shage?', name: 'stage']
            ])
}
