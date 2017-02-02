# CMakeDebugPlugin

## Environment Setup

1. Install [jdk](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html), add to path, set $JAVA_HOME env var
1. Download [gradle binaries](https://gradle.org/gradle-download/), add to path
1. Run `gradle build`, observe success
1. Import into IntelliJ IDEA

![import](img/import_project.png)

Select `build.gradle`:

![gradle](img/build_gradle.png)

Select these *very important* options:

![options](img/important_options.png)

Create a new `Run Configuration`:

![config](img/edit-run-config.png)

Select `More Items`:

![more](img/more-items.png)

Select `Plugin`:

![plugin](img/plugin.png)

Name the configuration `ImportantThisIsUnqiueName`:

![unique](img/unique.png)

Open `CMakeDebugPlugin.iws` from your root folder and search for `ImportantThisIsUnqiueName`:

![force](img/force-module.png)

Set the `<module name="" />` to be `<module name="CMakeDebugPlugin" />` close out of the `.iws` editor, and open `Run Configurations` to be sure it took effect:

![worked](img/worked.png)

Open `Module Settings`:

![settings](img/module_settings.png)

Set the `Project SDK` to be your IntelliJ version:

![sdk](img/project_sdk.png)

Hit `debug` and observe a second copy of IntelliJ has launched in debug mode:

![success](img/success.png)
