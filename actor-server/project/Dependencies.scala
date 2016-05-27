package im.actor

import sbt._

object Dependencies {
  object V {
    val actorCommons = "0.0.15"
    val actorBotkit = "1.0.109"
    val akka = "2.4.5"
    val akkaHttpJson = "1.5.0"
    val cats = "0.3.0"
    val circe = "0.2.1"
    val kamon = "0.5.2"
    val slick = "3.1.1"
    val slickPg = "0.10.2"
    val scalatest = "2.2.4"
    val shardakka = "0.1.24"
    val scalapbSer = "0.1.14"
  }

  object Compile {
    val actorConcurrent         = "im.actor"                      %% "actor-concurrent"              % V.actorCommons
    val actorUtil               = "im.actor"                      %% "actor-util"                    % V.actorCommons
    val actorCatsSlick          = "im.actor"                      %% "actor-cats-slick"              % V.actorCommons
    val actorBotkit             = "im.actor"                      %  "actor-botkit"                  % V.actorBotkit
    val shardakka               = "im.actor"                      %% "shardakka"                     % V.shardakka
    val scalapbSer              = "im.actor"                      %% "akka-scalapb-serialization"    % V.scalapbSer

    val akkaActor               = "com.typesafe.akka"             %% "akka-actor"                    % V.akka
    val akkaPersistence         = "com.typesafe.akka"             %% "akka-persistence"              % V.akka
    val akkaDdata               = "com.typesafe.akka"             %% "akka-distributed-data-experimental" % V.akka
    val akkaClusterTools        = "com.typesafe.akka"             %% "akka-cluster-tools"            % V.akka
    val akkaClusterSharding     = "com.typesafe.akka"             %% "akka-cluster-sharding"         % V.akka
    val akkaStream              = "com.typesafe.akka"             %% "akka-stream"                   % V.akka
    val akkaHttp                = "com.typesafe.akka"             %% "akka-http-experimental"        % V.akka
    val akkaHttpPlayJson        = "de.heikoseeberger"             %% "akka-http-play-json"           % V.akkaHttpJson
    val akkaHttpCirce           = "de.heikoseeberger"             %% "akka-http-circe"               % V.akkaHttpJson
    val akkaSlf4j               = "com.typesafe.akka"             %% "akka-slf4j"                    % V.akka

    val sprayClient             = "io.spray"                      %% "spray-client"                  % "1.3.3"
    val sprayWebsocket          = "com.wandoulabs.akka"           %% "spray-websocket"               % "0.1.4"

    val akkaPersistenceJdbc     = "com.github.dnvriend"           %% "akka-persistence-jdbc"         % "2.2.23"
    val apacheEmail             = "org.apache.commons"            %  "commons-email"                 % "1.4"

    val betterFiles             = "com.github.pathikrit"          %% "better-files"                  % "2.13.0"

    val caffeine                = "com.github.ben-manes.caffeine" %  "caffeine"                      % "2.2.7"

    val cats                    = "org.spire-math"                %% "cats"                          % V.cats

    val circeCore               = "io.circe"                      %% "circe-core"                    % V.circe
    val circeGeneric            = "io.circe"                      %% "circe-generic"                 % V.circe
    val circeParse              = "io.circe"                      %% "circe-parse"                   % V.circe

    val configs                 = "com.github.kxbmap"             %% "configs"                       % "0.3.0"

    val dispatch                = "net.databinder.dispatch"       %% "dispatch-core"                 % "0.11.3"
    val javaCompat              = "org.scala-lang.modules"        %% "scala-java8-compat"            % "0.7.0"

    val playJson                = "com.typesafe.play"             %% "play-json"                     % "2.4.2"
    val upickle                 = "com.lihaoyi"                   %% "upickle"                       % "0.3.6"

    val postgresJdbc            = "org.postgresql"                %  "postgresql"                    % "9.4.1208" exclude("org.slf4j", "slf4j-simple")
    val slick                   = "com.typesafe.slick"            %% "slick"                         % V.slick
    val slickHikaricp           = "com.typesafe.slick"            %% "slick-hikaricp"                % V.slick exclude("com.zaxxer", "HikariCP-java6")
    val slickJoda               = "com.github.tototoshi"          %% "slick-joda-mapper"             % "2.0.0"
    val slickPg                 = "com.github.tminglei"           %% "slick-pg"                      % V.slickPg
    val slickPgDate2            = "com.github.tminglei"           %% "slick-pg_date2"                % V.slickPg
    val slickTestkit            = "com.typesafe.slick"            %% "slick-testkit"                 % V.slick
    val flywayCore              = "org.flywaydb"                  %  "flyway-core"                   % "3.1"
    val hikariCP                = "com.zaxxer"                    %  "HikariCP"                      % "2.4.6"

    val amazonaws               = "com.amazonaws"                 %  "aws-java-sdk-s3"               % "1.9.31"
    val awsWrap                 = "com.github.dwhjames"           %% "aws-wrap"                      % "0.7.2"

    val bcprov                  = "org.bouncycastle"              %  "bcprov-jdk15on"                % "1.50"

    val kamonCore               = "io.kamon"                      %% "kamon-core"                    % V.kamon
    val kamonDatadog            = "io.kamon"                      %% "kamon-datadog"                 % V.kamon

    val libPhoneNumber          = "com.googlecode.libphonenumber" % "libphonenumber"                 % "7.0.+"
    val icu4j                   = "com.ibm.icu"                   % "icu4j"                          % "56.1"

    val scodecBits              = "org.scodec"                    %% "scodec-bits"                   % "1.0.9"
    val scodecCore              = "org.scodec"                    %% "scodec-core"                   % "1.8.1"

    val scopt                   = "com.github.scopt"              %% "scopt"                         % "3.3.0"

    val shapeless               = "com.chuusai"                   %% "shapeless"                     % "2.2.4"

    val scrImageCore            = "com.sksamuel.scrimage"         %% "scrimage-core"                 % "2.1.0"

    val tyrex                   = "tyrex"                         %  "tyrex"                         % "1.0.1"

    val pushy                   = "com.relayrides"                %  "pushy"                         % "0.6.1"

    val logbackClassic          = "ch.qos.logback"                % "logback-classic"                % "1.1.2"
    val scalaLogging            = "com.typesafe.scala-logging"    %% "scala-logging"                 % "3.1.0"

    val jodaTime                = "joda-time"                     %  "joda-time"                     % "2.7"
    val jodaConvert             = "org.joda"                      %  "joda-convert"                  % "1.7"

    val apacheCommonsCodec      = "commons-codec"                 % "commons-codec"                  % "1.10"
    val apacheCommonsIo         = "commons-io"                    % "commons-io"                     % "2.4"
    val apacheCommonsValidator  = "commons-validator"             % "commons-validator"              % "1.4.1"

    val guava                   = "com.google.guava"              % "guava"                          % "19.0"
    val alpn                    = "org.eclipse.jetty.alpn"        % "alpn-api"                       % "1.1.2.v20150522" % "runtime"
    val tcnative                = "io.netty"                      % "netty-tcnative"                 % "1.1.33.Fork15" classifier "linux-x86_64"
  }

  object Testing {
    val akkaTestkit             = "com.typesafe.akka"             %% "akka-testkit"                  % V.akka
    val akkaMultiNodeTestkit    = "com.typesafe.akka"             %% "akka-multi-node-testkit"       % V.akka

    val scalacheck      = "org.scalacheck"                        %% "scalacheck"                    % "1.12.5"
    val scalatest       = "org.scalatest"                         %% "scalatest"                     % V.scalatest

    val jfairy          = "io.codearte.jfairy"                    %  "jfairy"                        % "0.3.1"
  }

  import Compile._
  import Testing._

  val shared = Seq(
    alpn,
    tcnative,
    configs,
    actorUtil,
    javaCompat,
    logbackClassic,
    scalaLogging,
    tyrex,
    kamonCore,
    kamonDatadog
  )

  val root = shared ++ Seq(
    akkaSlf4j, akkaActor, akkaStream
  )

  val activation = shared ++ Seq(akkaActor, playJson, sprayClient)

  val bots = shared ++ Seq(actorBotkit, upickle, shardakka)

  val botkit = Seq(actorConcurrent, akkaActor, akkaHttp, akkaSlf4j, javaCompat, sprayWebsocket, upickle)

  val botShared = Seq(upickle, javaCompat)

  val cli = Seq(akkaClusterTools, scopt)

  val core = shared ++ Seq(
    actorConcurrent,
    akkaActor,
    akkaClusterTools,
    akkaClusterSharding,
    akkaDdata,
    caffeine,
    pushy,
    jodaTime,
    postgresJdbc,
    shardakka,
    scrImageCore,
    sprayClient
  )

  val enrich = shared ++ Seq(akkaActor, akkaHttp)

  val rpcApi = shared ++ Seq(
    akkaSlf4j, akkaActor, bcprov, apacheCommonsIo, apacheCommonsValidator, shapeless, akkaHttpPlayJson
  )

  val httpApi = shared ++ Seq(akkaActor, akkaHttp, akkaHttpPlayJson, akkaHttpCirce, circeCore, circeGeneric, circeParse, jodaTime, playJson)

  val email = shared ++ Seq(akkaActor, apacheEmail)

  val oauth = shared ++ Seq(akkaActor, akkaHttp, playJson)

  val session = shared ++ Seq(
    akkaSlf4j, akkaActor, akkaStream, scodecCore
  )

  val sessionMessages = Seq(akkaActor)

  val persist = shared ++ Seq(akkaActor, akkaStream, actorCatsSlick, apacheCommonsCodec, guava, postgresJdbc, slick, slickHikaricp, slickJoda, slickPg, slickPgDate2, slickTestkit, flywayCore, hikariCP, jodaTime, jodaConvert)

  val presences = shared :+ akkaClusterSharding

  val sms = shared ++ Seq(akkaActor, akkaHttp, dispatch)

  val codecs = shared ++ Seq(scodecBits, scodecCore)
  
  val models = shared ++ Seq(scodecBits, scodecCore, jodaTime, jodaConvert, slickPg)

  val fileAdapter = shared ++ Seq(amazonaws, apacheCommonsCodec, apacheCommonsIo, awsWrap, betterFiles)

  val frontend = shared ++ Seq(
    akkaSlf4j, akkaActor, akkaStream,
    guava,
    scodecBits, scodecCore
  )

  val sdk = Seq.empty

  val runtime = shared ++ Seq(akkaActor, actorConcurrent, akkaHttp, akkaSlf4j, akkaStream, akkaPersistenceJdbc, apacheCommonsCodec, caffeine, cats, jodaConvert, jodaTime, icu4j, libPhoneNumber, scalapbSer, akkaTestkit % "test", scalatest % "test")

  val voximplant = shared ++ Seq(akkaActor, dispatch, playJson)

  val tests = shared ++ Seq(akkaClusterSharding, amazonaws, jfairy, scalacheck, scalatest, slickTestkit, akkaTestkit, akkaMultiNodeTestkit)
}
