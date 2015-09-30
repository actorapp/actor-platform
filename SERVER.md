# Building and Deploying the server

The Actor Platform is separated into server software and client applications.

## Docker (multi-platform)

Read more in our [new documentation](http://actor.readme.io/docs/server-docker)

## Ubuntu (tested on 14.04)

```
echo "deb https://dl.bintray.com/actor/ubuntu trusty main" | sudo tee -a /etc/apt/sources.list
sudo apt-get update
sudo apt-get install actor
```

create `/etc/actor/server.conf` (see `/etc/actor/server.conf.example`)

## Building the server (optional)
* Install ```java``` and ```sbt``` on your machine.
* Build server by calling ```sbt dist``` (first build may take up to 10 min).
* Grab your server dist zip in ```actor-server/target/universal```.

## Configuring the server

### Requirements
* Installed PostgreSQL version 9.3+
* AWS account with S3 bucket for file storage
* Account with one of the following SMS gateways: TeleSign, Clickatell, or Twilio
* Have SSL certificates and public IP (or two for Mobile and Web clients)

### Configuration file

Before starting the server, you need a server.conf file from the ```config``` directory. See the [application.conf.example](actor-server/src/main/resources/application.conf.example) or [application.conf.example-minimal](actor-server/src/main/resources/application.conf.example-minimal) file for an example.

## Starting the server

To start the server, unpack dist, and from its root run ```bin/actor-server```.

## Mobile Apps configuration

### Requirements
* Project in [Google Developer Console](https://console.developers.google.com)
  * Push Configuration
* An [Apple Developer Account](https://developer.apple.com)
  * Push Configuration

After meeting the requirements, you need to update your server.conf file with all required settings and restart the server.

Once the server is running, you can start to [build your apps](APPS.md).
