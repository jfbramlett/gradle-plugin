# HBO-COMMON-GRADLE-BUILDINFO

This repository contains the code for generating a buildinfo.json file as part of a gradle build. The contents of this generated file are read in by a bean and used to enrich Spring's /info endpoint on a microservice. 

The data captured by this plugin looks something like:

```
{
    app: {
        name: <project.name> and can be overridden via -DappName
        description: <project.description> and can be overridden via -DappDescription
        version: -DappVersion
    }
    git: {
        branch: "master"
        remote: "https://git.homebox.com/scm/com/hbo-common-buildinfo.git"
        commit: {
            id: "dd59c7c3483afb77f86640a026f59e6cd34ef27e"
            user: "jon.snow@hbo.com"
            message:  "Updated to use json for build info"
            timestamp: "2016-09-27T17:19:18.000-04:00"
        }
    }, 
    build: {
        number: 1089
        revision: 2
        timestamp: "2016-09-27T17:19:18.000-04:00"
        user: "jon.snow@hbo.com"
    }
}
```

To use the plugin add the following to your build.gradle:

```
buildscript {
    repositories {
        ...
    }
    dependencies {
        ...
        classpath "com.hbo.common:gradle-buildinfo-plugin:1.0-SNAPSHOT"
        ...
    }
}

...

apply plugin: 'common5-buildinfo'

```

And when executing your build on Bamboo add the following -D parameters:

```
-PappVersion=${bamboo.appversion} -PbuildNumber=${bamboo.buildNumber} -PbuildKey=${bamboo.buildKey} -PbuildTimestamp=${bamboo.buildTimeStamp} -PbuildTriggerUser=${bamboo.ManualBuildTriggerReason.userName} -PbuildPlanName="${bamboo.planName}" -PplanRepository=${bamboo.planRepository.repositoryUrl}
```