package io.skysail.jenkins.config

enum Environment {
    TEST("test"), INT("Int"), PRD("prd")

    private String name

    Environment(final String name) {
        this.name = name
    }

    String getName() {
        return name
    }
}
