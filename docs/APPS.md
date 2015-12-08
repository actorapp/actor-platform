# Building Mobile applications

Before starting to build your applications, you must configure your server to [support mobile applications](SERVER.md).

We support OS X and Ubuntu environments out of the box, but you can easily install all the dependencies on other GNU/Linux or Windows systems and build the apps.

# Prepare Environment

For your convenience, we have a script that can download and install all required dependencies for building your new app. Just run ```./installDeps.sh``` in the ```actor-apps``` directory.

## Android Dependencies

The Android application only needs to have the Android SDK installed. You can just open the gradle project in the ```actor-apps``` directory, change the server endpoints, and run your app.

## Web Dependencies

The Web application needs to have ```gulp```, ```node/npm```, and ```bower``` installed on the local machine. After installing the dependencies manually (or with ```./installDeps.sh```) you can run ```gulp dev``` in the ```actor-apps/app-web``` directory.

## iOS Dependencies

The iOS application needs to have ```j2objc``` and ```cocoapods``` installed on the local machine. You can open the project with Xcode in the ```actor-apps/actor-ios``` directory.
