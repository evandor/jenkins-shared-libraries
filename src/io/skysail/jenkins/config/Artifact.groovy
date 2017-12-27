package io.skysail.jenkins.config

class Artifact {

    String group, artifactId, packagingType

    Artifact(final String group, final String artifactId, String packagingType) {
        this.group = group
        this.artifactId = artifactId
        this.packagingType = packagingType
    }

}

