
def call(application) {
    env.APPLICATION_VERSION =
            input(id: 'env.APPLICATION_VERSION', message: 'Welche Version soll deployed werden?', parameters: [
                    [$class       : 'StringParameterDefinition',
                     defaultValue : "0.0.xxx",
                     description  : 'Welche Version soll deployed werden?', name: 'Enter the version']
            ])
}
