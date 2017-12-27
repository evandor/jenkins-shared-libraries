package io.skysail.jenkins.config

class ApplicationRepo {

    static applications = [:] as HashMap

    static {
        applications = [:] as HashMap
        applications.put("an-app", new Artifact("io.skysail", "an-app", "bundle"))
    }

    static Application getApplicationByKey(key) {
        return (Application)applications.get(key);
    }

    static Set getApplicationKeysByInstance(instance) {
        return applications.keySet().findAll {applications.get(it).hasInstance(instance)}
    }

    static String getApplicationChoices(instance) {
        return getApplicationKeysByInstance(instance).join('\n')
    }
}

