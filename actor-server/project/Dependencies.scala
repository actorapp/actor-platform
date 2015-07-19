package im.actor

import sbt._

object Dependencies {
  object V {
    val akka = "2.3.12"
    val akkaExperimental = "1.0"
    val scalaz = "7.1.1"
    val slick = "3.0.0"
    val scalatest = "2.2.4"
  }

  object Compile {
    val akkaActor               = "com.typesafe.akka"             %% "akka-actor"                    % V.akka
    val akkaContrib             = "com.typesafe.akka"             %% "akka-contrib"                  % V.akka
    val akkaKernel              = "com.typesafe.akka"             %% "akka-kernel"                   % V.akka
    val akkaStream              = "com.typesafe.akka"             %% "akka-stream-experimental"      % V.akkaExperimental
    val akkaHttp                = "com.typesafe.akka"             %% "akka-http-experimental"        % V.akkaExperimental
    val akkaHttpCore            = "com.typesafe.akka"             %% "akka-http-core-experimental"   % V.akkaExperimental
    val akkaHttpSpray           = "com.typesafe.akka"             %% "akka-http-spray-json-experimental" % V.akkaExperimental
    val akkaSlf4j               = "com.typesafe.akka"             %% "akka-slf4j"                    % V.akka

    val akkaPersistenceKafka    = "com.github.krasserm"           %% "akka-persistence-kafka"        % "0.3.4" exclude("org.slf4j", "slf4j-log4j12")
    val akkaPersistenceJdbc     = "com.github.dnvriend"           %% "akka-persistence-jdbc"         % "1.1.6"
    val apacheEmail             = "org.apache.commons"            %  "commons-email"                 % "1.4"

    val concmap                 = "com.googlecode.concurrentlinkedhashmap" % "concurrentlinkedhashmap-lru" % "1.4.2"
    val caffeine                = "com.github.ben-manes.caffeine" %  "caffeine"                      % "1.2.0"
    val eaioUuid                = "com.eaio.uuid"                 %  "uuid"                          % "3.4"

    val configs                 = "com.github.kxbmap"             %% "configs"                       % "0.2.4"

    val dispatch                = "net.databinder.dispatch"       %% "dispatch-core"                 % "0.11.2"
    val javaCompat              = "org.scala-lang.modules"        %% "scala-java8-compat"            % "0.5.0"

    @deprecated("use `playJson` instead")
    val sprayJson               = "io.spray"                      %% "spray-json"                    % "1.3.1"
    val playJson                = "com.typesafe.play"             %% "play-json"                     % "2.4.1"

    val postgresJdbc            = "org.postgresql"                %  "postgresql"                    % "9.4-1200-jdbc41" exclude("org.slf4j", "slf4j-simple")
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

    val akkaKryoSerialization   = "com.github.romix.akka"         %% "akka-kryo-serialization"       % "0.3.3"
    val kryoSerializers         = "de.javakaffee"                 %  "kryo-serializers"              % "0.29"

    val protobuf                = "com.google.protobuf"           %  "protobuf-java"                 % "2.6.1"

    val scodecBits              = "org.scodec"                    %% "scodec-bits"                   % "1.0.5"
    val scodecCore              = "org.scodec"                    %% "scodec-core"                   % "1.7.0"

    val scalazCore              = "org.scalaz"                    %% "scalaz-core"                   % V.scalaz
    val scalazConcurrent        = "org.scalaz"                    %% "scalaz-concurrent"             % V.scalaz

    val shapeless               = "com.chuusai"                   %% "shapeless"                     % "2.1.0"

    val scrImageCore            = "com.sksamuel.scrimage"         %% "scrimage-core"                 % "1.4.2"

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
    val akkaTestkit             = "com.typesafe.akka"             %% "akka-testkit"                  % V.akka % "test"

    val scalacheck      = "org.scalacheck"                        %% "scalacheck"                    % "1.12.2" % "test"
    val scalatest       = "org.scalatest"                         %% "scalatest"                     % V.scalatest % "test"
//    val scalaTestPlay   = "org.scalatestplus"                     %% "play"                          % "1.2.0" % "test"

    val jfairy          = "io.codearte.jfairy"                    %  "jfairy"                        % "0.3.1" % "test"
  }

  import Compile._
  import Testing._

  val shared = Seq(javaCompat, logbackClassic, scalaLogging, configs)

  val root = shared ++ Seq(
    akkaSlf4j, akkaActor, akkaKernel, akkaStream
  )

  val activation = shared ++ Seq(akkaActor, akkaHttp, playJson)

  val commonsBase = shared ++ Seq(akkaActor, akkaPersistenceKafka, akkaPersistenceJdbc, akkaKryoSerialization, concmap, jodaConvert, jodaTime, kryoSerializers)

  val commonsApi = shared ++ Seq(akkaSlf4j, akkaActor, akkaStream, apacheCommonsCodec, protobuf, scalazCore)

  val enrich = shared ++ Seq(akkaActor, akkaHttp)

  val rpcApi = shared ++ Seq(
    akkaSlf4j, akkaActor, amazonaws, awsWrap, bcprov, apacheCommonsIo, shapeless
  )

  val httpApi = shared ++ Seq(akkaActor, jodaTime, akkaHttp, playJson)

  val email = shared ++ Seq(akkaActor, apacheEmail)

  val llectro = shared ++ Seq(akkaActor, akkaHttpCore, akkaHttp, akkaStream, playJson)

  val internalServices = shared ++ Seq(akkaActor, akkaStream, scodecBits)

  val oauth = shared ++ Seq(akkaActor, akkaHttp, playJson)

  val session = shared ++ Seq(
    akkaSlf4j, akkaActor, akkaKernel, akkaStream, scodecCore
  )

  val sessionMessages = Seq(akkaActor)

  val push = shared ++ Seq(akkaContrib, gcmServer, pushy)

  val peerManagers = shared ++ Seq(akkaActor, akkaContrib, jodaTime, postgresJdbc, slick)

  val persist = shared ++ Seq(apacheCommonsCodec, postgresJdbc, slick, slickJoda, slickPg, slickTestkit, flywayCore, hikariCP, jodaTime, jodaConvert)

  val presences = shared :+ akkaContrib

  val sms = shared ++ Seq(akkaActor, akkaHttp, dispatch)

  val social = shared :+ akkaContrib

  val tls = shared ++ Seq(akkaHttp, akkaStream)

  val codecs = shared ++ Seq(scalazCore, scodecBits, scodecCore)
  
  val models = shared ++ Seq(eaioUuid, scodecBits, scodecCore, sprayJson, jodaTime, jodaConvert, slickPg)

  val frontend = shared ++ Seq(
    akkaSlf4j, akkaActor, akkaKernel, akkaStream,
    scodecBits, scodecCore,
    scalazCore, scalazConcurrent
  )

  val dashboard = shared :+ scalazCore

  val notifications = shared ++ Seq(akkaContrib, slick)

  val utils = shared ++ Seq(akkaActor, akkaHttp, amazonaws, awsWrap, libPhoneNumber, scrImageCore, slick)

  val utilsCache = shared :+ caffeine

  val utilsHttp = shared ++ Seq(akkaActor, akkaHttp, akkaTestkit, scalatest)

  val voximplant = shared ++ Seq(akkaActor, dispatch, playJson)

  val tests = shared ++ Seq(
    jfairy, scalacheck, scalatest, slickTestkit, akkaTestkit //, scalaTestPlay
  )
}
