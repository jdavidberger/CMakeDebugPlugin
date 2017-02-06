# CMakeDebugPlugin

## Environment Setup for source build

1. Install [jdk](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html), add to path, set $JAVA_HOME env var
1. Download [gradle binaries](https://gradle.org/gradle-download/), add to path
1. Run `gradle build`, observe success
1. Run `gradle runIdea` to launch a sand box Intellij IDE

## Get pre-built binaries

[They are available here](https://github.com/jdavidberger/CMakeDebugPlugin/releases)

## Installation from zip

Go into 'Settings' -> 'Plugins' -> 'Install plugin from disk...' and find the plugin zip file. This will require a restart of the IDE. 

## Get a version of CMake with the debugger feature

You can get binaries [here](https://github.com/jdavidberger/CMake/releases). You can build from source from [here](https://github.com/jdavidberger/CMake/tree/debugger). The plugin requires a version of CMake with the debugger integrated to work. 

## Usage

After installation, go into 'Edit Configurations', click the '+' button in the top left and add the 'CMake' application type (you might have to click to show more options in the dropdown). 

On the configuration panel, only modify the port if you have a conflict with something else running on your system. 

Modify the 'CMake Installation' option to point to a version of CMake with the integrated debugger. 

You can set the build and source directory to wherever the project you want to build is. If you leave them empty, you'll get reasonable defaults -- the source directory will be the project root, and the build directory will be a build folder hanging off of that folder. 

After that, select your newly configured configuration and hit the normal 'debug' button. This should start into the normal debug mode. Note that the plugin also gives you the ability to set breakpoints in CMake files. 

'Add watch' can be used to display variables like in most other languages -- so CMAKE_GENERATOR would show you the current generator. You can also put a string into the watch to have it evaluate the string as CMAKE would. So if you added "${CMAKE_GENERATOR} ${CMAKE_CURRENT_SOURCE_DIR}" it would evaluate that as if you sent it to MESSAGE. This can be arbitrarily nested; basically anything you can put in MESSAGE should show up here. 
