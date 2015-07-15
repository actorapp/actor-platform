# Prepare for build server and deploy

Actor Platform is separated to server software and client applications.

## Easy way: Docker

Read more in our [new documentation](http://actor.readme.io/docs/server-docker)

## Building Server (optional)
* Install ```java``` and ```sbt``` on your machine.
* Build server by calling ```sbt dist``` (first build may take up to 10 min)
* Grab your server at ```actor-server/target/dist``` directory

## Configure Server

### Requirements for server
* Installed PostgreSQL version 9.3+
* Installed Apache Kaffka
* Installed Apache ZooKeeper
* AWS account with S3 bucket for file storage
* Account at SMS gate one of: Telesign, Clicatell or Twillio
* Have SSL Sertificates and public IP (or two for Mobile and Web clients)
 
### Configuration file

Before starting file you need to application.conf file from [application.conf.example](actor-server/src/main/resources/application.conf.example) file in ```config``` directory

## Start server

To run server run ```bin/start```

## Mobile Apps configuration
### Requirements for mobile support
* Project in [Google Developer Console](https://console.developers.google.com)
  * Push Configuration
* [Apple Developer Account](https://developer.apple.com)
  * Push Configuration

After requirements you need to update your application.conf file and provide all required settings and restart your server.

When server is running you can start to [build your apps](APPS.md).
