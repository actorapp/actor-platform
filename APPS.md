# Building Mobile applications

Before starting building your applications you need to have configured server to [support mobile applications](SERVER.md).

We support OSX and Ubuntu environments out of the box, but you can easily install all dependencies on other GNU/Linux or Widnows systems and build apps.

# Prepare environment

For easier to use we have script that can download and install all required dependencies for building your new app. Just run ```./installDeps.sh``` in ```actor-apps``` directory.

## Android Dependencies

Android application only need to have Android SDK installed. Right after checkout you cat open gradle project from ```actor-apps``` directory and after changing server endpoints, you can run your app.

## Web Dependencies

Web application need to have ```gulp```, ```node/npm``` and ```bower``` installed on local machine. After installing dependencies manually or with ```./installDeps.sh``` you can run ```gulp dev``` in ```actor-apps/app-web``` directory.

## iOS Dependencies

iOS application need to have ```j2objc``` and ```cocoapods``` installed on local machine and you can start your project from XCode from ```actor-apps/actor-ios``` directory.
