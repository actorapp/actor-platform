package im.actor

import sbt._

object Dependencies {
  object V {
    val actorCommons = "0.0.7"
    val akka = "2.4.0"
    val akkaExperimental = "1.0"
    val cats = "0.2.0"
    val scalaz = "7.1.1"
    val slick = "3.0.3"
    val scalatest = "2.2.4"
    val shardakka = "0.1.12"
    val scalapbSer = "0.1.6"
  }

  object Compile {
    val actorConcurrent         = "im.actor"                      %% "actor-concurrent"              % V.actorCommons
    val shardakka               = "im.actor"                      %% "shardakka"                     % V.shardakka
    val scalapbSer              = "im.actor"                      %% "akka-scalapb-serialization"    % V.scalapbSer

    val akkaActor               = "com.typesafe.akka"             %% "akka-actor"                    % V.akka
    val akkaPersistence         = "com.typesafe.akka"             %% "akka-persistence"              % V.akka
    val akkaClusterTools        = "com.typesafe.akka"             %% "akka-cluster-tools"            % V.akka
    val akkaClusterSharding     = "com.typesafe.akka"             %% "akka-cluster-sharding"         % V.akka
    val akkaStream              = "com.typesafe.akka"             %% "akka-stream-experimental"      % V.akkaExperimental
    val akkaHttp                = "com.typesafe.akka"             %% "akka-http-experimental"        % V.akkaExperimental
    val akkaHttpCore            = "com.typesafe.akka"             %% "akka-http-core-experimental"   % V.akkaExperimental
    val akkaHttpPlayJson        = "de.heikoseeberger"             %% "akka-http-play-json"           % "1.0.0"
    val akkaSlf4j               = "com.typesafe.akka"             %% "akka-slf4j"                    % V.akka

    val sprayWebsocket          = "com.wandoulabs.akka"           %% "spray-websocket"               % "0.1.4"

    val akkaPersistenceJdbc     = "com.github.dnvriend"           %% "akka-persistence-jdbc"         % "1.2.0"
    val apacheEmail             = "org.apache.commons"            %  "commons-email"                 % "1.4"

    val concmap                 = "com.googlecode.concurrentlinkedhashmap" % "concurrentlinkedhashmap-lru" % "1.4.2"
    val caffeine                = "com.github.ben-manes.caffeine" %  "caffeine"                      % "1.2.0"
    val eaioUuid                = "com.eaio.uuid"                 %  "uuid"                          % "3.4"

    val cats                    = "org.spire-math"                %% "cats"                          % V.cats

    val configs                 = "com.github.kxbmap"             %% "configs"                       % "0.2.4"

    val dispatch                = "net.databinder.dispatch"       %% "dispatch-core"                 % "0.11.2"
    val javaCompat              = "org.scala-lang.modules"        %% "scala-java8-compat"            % "0.5.0"

    val playJson                = "com.typesafe.play"             %% "play-json"                     % "2.4.2"
    val upickle                 = "com.lihaoyi"                   %% "upickle"                       % "0.3.6"

    val postgresJdbc            = "org.postgresql"                %  "postgresql"                    % "9.4-1201-jdbc41" exclude("org.slf4j", "slf4j-simple")
    val slick                   = "com.typesafe.slick"            %% "slick"                         % V.slick
    val slickJoda               = "com.github.tototoshi"          %% "slick-joda-mapper"             % "2.0.0"
    val slickPg                 = "com.github.tminglei"           %% "slick-pg"                      % "0.9.0"
    val slickTestkit            = "com.typesafe.slick"            %% "slick-testkit"                 % V.slick
    val flywayCore              = "org.flywaydb"                  %  "flyway-core"                   % "3.1"
    val hikariCP                = "com.zaxxer"                    %  "HikariCP"                      % "2.3.5"

    val amazonaws               = "com.amazonaws"                 %  "aws-java-sdk-s3"               % "1.9.31"
    val awsWrap                 = "com.github.dwhjames"           %% "aws-wrap"                      % "0.7.2"

    val bcprov                  = "org.bouncycastle"              %  "bcprov-jdk15on"                % "1.50"

    val libPhoneNumber          = "com.googlecode.libphonenumber" % "libphonenumber"                 % "7.0.+"

    val protobuf                = "com.google.protobuf"           %  "protobuf-java"                 % "3.0.0-alpha-3"

    val scodecBits              = "org.scodec"                    %% "scodec-bits"                   % "1.0.9"
    val scodecCore              = "org.scodec"                    %% "scodec-core"                   % "1.8.1"

    val scalazCore              = "org.scalaz"                    %% "scalaz-core"                   % V.scalaz
    val scalazConcurrent        = "org.scalaz"                    %% "scalaz-concurrent"             % V.scalaz

    val shapeless               = "com.chuusai"                   %% "shapeless"                     % "2.2.4"

    val scrImageCore            = "com.sksamuel.scrimage"         %% "scrimage-core"                 % "1.4.2"

    val tyrex                   = "tyrex"                         %  "tyrex"                         % "1.0.1"

    val gcmServer               = "com.google.android.gcm"        %  "gcm-server"                    % "1.0.2"
    val pushy                   = "com.relayrides"                %  "pushy"                         % "0.4.3"

    val logbackClassic          = "ch.qos.logback"                % "logback-classic"                % "1.1.2"
    val scalaLogging            = "com.typesafe.scala-logging"    %% "scala-logging"                 % "3.1.0"

    val jodaTime                = "joda-time"                     %  "joda-time"                     % "2.7"
    val jodaConvert             = "org.joda"                      %  "joda-convert"                  % "1.7"

    val apacheCommonsCodec      = "commons-codec"                 % "commons-codec"                  % "1.10"
    val apacheCommonsIo         = "commons-io"                    % "commons-io"                     % "2.4"
  }

  object Testing {
    val akkaTestkit             = "com.typesafe.akka"             %% "akka-testkit"                  % V.akka

    val scalacheck      = "org.scalacheck"                        %% "scalacheck"                    % "1.12.5"
    val scalatest       = "org.scalatest"                         %% "scalatest"                     % V.scalatest

    val jfairy          = "io.codearte.jfairy"                    %  "jfairy"                        % "0.3.1"
  }

  import Compile._
  import Testing._

  val shared = Seq(configs, javaCompat, logbackClassic, scalaLogging, tyrex)

  val root = shared ++ Seq(
    akkaSlf4j, akkaActor, akkaStream
  )

  val activation = shared ++ Seq(akkaActor, akkaHttp, akkaHttpPlayJson, playJson)

  val bots = shared ++ Seq(upickle, shardakka)

  val botkit = Seq(actorConcurrent, akkaActor, akkaHttp, akkaSlf4j, javaCompat, sprayWebsocket, upickle)

  val botShared = Seq(upickle)

  val core = shared ++ Seq(
    actorConcurrent,
    akkaActor,
    akkaClusterTools,
    akkaClusterSharding,
    amazonaws,
    awsWrap,
    caffeine,
    gcmServer,
    pushy,
    jodaTime,
    postgresJdbc,
    shardakka,
    slick,
    scrImageCore)

  val enrich = shared ++ Seq(akkaActor, akkaHttp)

  val rpcApi = shared ++ Seq(
    akkaSlf4j, akkaActor, amazonaws, awsWrap, bcprov, apacheCommonsIo, shapeless, akkaHttpPlayJson
  )

  val httpApi = shared ++ Seq(akkaActor, akkaHttp, akkaHttpPlayJson, jodaTime, playJson)

  val email = shared ++ Seq(akkaActor, apacheEmail)

  val oauth = shared ++ Seq(akkaActor, akkaHttp, playJson)

  val session = shared ++ Seq(
    akkaSlf4j, akkaActor, akkaStream, scodecCore
  )

  val sessionMessages = Seq(akkaActor)

  val persist = shared ++ Seq(akkaActor, apacheCommonsCodec, postgresJdbc, slick, slickJoda, slickPg, slickTestkit, flywayCore, hikariCP, jodaTime, jodaConvert)

  val presences = shared :+ akkaClusterSharding

  val sms = shared ++ Seq(akkaActor, akkaHttp, dispatch)

  val social = shared :+ akkaClusterSharding

  val codecs = shared ++ Seq(scalazCore, scodecBits, scodecCore)
  
  val models = shared ++ Seq(eaioUuid, scodecBits, scodecCore, jodaTime, jodaConvert, slickPg)

  val frontend = shared ++ Seq(
    akkaSlf4j, akkaActor, akkaStream,
    scodecBits, scodecCore,
    scalazCore, scalazConcurrent
  )

  val dashboard = shared :+ scalazCore

  val notifications = shared ++ Seq(akkaClusterTools, slick)

  val runtime = shared ++ Seq(akkaActor, akkaHttp, akkaStream, akkaPersistenceJdbc, caffeine, cats, concmap, jodaConvert, jodaTime, libPhoneNumber, scalapbSer, scalazCore, akkaTestkit % "test", scalatest % "test")

  val voximplant = shared ++ Seq(akkaActor, dispatch, playJson)

  val tests = shared ++ Seq(akkaClusterSharding, jfairy, scalacheck, scalatest, slickTestkit, akkaTestkit)
}
