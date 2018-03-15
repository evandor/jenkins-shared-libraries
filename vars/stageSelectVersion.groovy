
def call(application) {
    env.VERSION =
            input(id: 'env.VERSION', message: 'Welche Version soll deployed werden?', parameters: [
                    [$class       : 'StringParameterDefinition',
                     defaultValue : "0.0.xxx",
                     description  : 'Welche Version soll deployed werden?', name: 'Enter the version']
            ])
}
