
def call() {
    env.CONTINUE_TO_PROD =
            input(
                    id: 'env.CONTINUE_TO_PROD', message: 'Deploy to Prod?', parameters: [
                    [$class: 'BooleanParameterDefinition', defaultValue: false, description: '', name: 'Deploy to prod?']
            ])


}
