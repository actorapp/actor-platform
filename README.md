# Actor Client Applications
This repo contains all client applications source code. Most part of this repo is intended to be open source, but some part are required to be closed source, so this repo is not intended to be released directly.


## Structure
Repo is separated to main library projects and app projects. Library contains all client side application logic, so apps need to implement only UI part and some simple providers.

### Library projects
* ```actor-common```: core of Actor Messenger is implemented in java 6 without any use of reflection
* ```actor-common-base```: extension of actor-common for enviroments with true multithreading (DisplayLists support)
* ```actor-bc```: subset of Bouncy Castle for supporting of crypto
* ```actor-jvm```: extension of actor-common-base that adds some jvm-specific implementations (networking and logging)
* ```actor-js```: GWT application for building java script library for working with Actor API
* ```actor-cocoa-base```: Generation of sources for apple platforms with j2ObjC toolkit
* ```actor-android-base```: extension of actor-jvm that adds some android-specific implementations (main thread, log, file system, crypto provider, better sending)

### Application projects
* ```actor-android```: Android Client of Actor Messenger
* ```actor-ios```: iOS Client of Actor Messenger
* ```actor-web```: Web Client of Actor Messenger

## Using repo
For working with repo you need to pull all sources and follow instructions in folder of project you need.

## Build Machine requirements
All compilation required to be executed on Mac OS.
Following requirements are set for sources:
* **XCode 6.3+**: Required for iOS apps and required for Homebrew installation
* **Homebrew package manager**: http://brew.sh/ - Required for installing all required dependencies
* **j2ObjC v0.9.6.1**: [Download release on GitHub](https://github.com/google/j2objc/releases) - Used for conversion of actor-common to Objective-C
* **NodeJS package manager**: ```brew install npm``` - Used for Web/Electron apps
* **Bower**: ```npm install -g bower``` - Used for Web/Electron apps
* **Gulp**: ```npm install -g gulp``` - Build system for Web/Electorn app
* **JDK**: [Download from Oracle](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) - Required for building java code
* **Android SDK**: [Download release](http://developer.android.com/sdk/installing/index.html?pkg=tools) - Used for building Android and Library code
* **Valid Release Android Key**: Required for building android release app
* **Valid iOS Provision and Certificate**: Required for building iOS release app
* **Atom Electron Release distribution**: Required for building desktop apps
