import sbt._

object Dependencies {
  object V {
    val akka = "2.3.9"
    val akkaExperimental = "1.0-M3"
    val scalaz = "7.1.1"
    val slick = "3.0.0-RC1"
  }

  object Compile {
    val akkaActor       = "com.typesafe.akka"             %% "akka-actor"                    % V.akka
    val akkaKernel      = "com.typesafe.akka"             %% "akka-kernel"                   % V.akka
    val akkaStream      = "com.typesafe.akka"             %% "akka-stream-experimental"      % V.akkaExperimental
    val akkaHttp        = "com.typesafe.akka"             %% "akka-http-experimental"        % V.akkaExperimental
    val akkaHttpCore    = "com.typesafe.akka"             %% "akka-http-core-experimental"   % V.akkaExperimental
    val akkaHttpSpray   = "com.typesafe.akka"             %% "akka-http-spray-json-experimental" % V.akkaExperimental
    val akkaSlf4j       = "com.typesafe.akka"             %% "akka-slf4j"                    % V.akka
    val akkaStreamWS    = "pt"                            %% "akka-stream-websocket"         % "0.1-SNAPSHOT"

    val sprayJson       = "io.spray"                      %% "spray-json"                    % "1.3.1"

    val postgresJdbc    = "org.postgresql"                %  "postgresql"                    % "9.4-1200-jdbc41" exclude("org.slf4j", "slf4j-simple")
    val slick           = "com.typesafe.slick"            %% "slick"                         % V.slick
    val slickJoda       = "com.github.tototoshi"          %% "slick-joda-mapper"             % "1.3.0-SNAPSHOT"
    val flywayCore      = "org.flywaydb"                  %  "flyway-core"                   % "3.1"
    val hikariCP        = "com.zaxxer"                    %  "HikariCP"                      % "2.3.2"

    val bcprov          = "org.bouncycastle"              %  "bcprov-jdk15on"                % "1.50"

    val libPhoneNumber  = "com.googlecode.libphonenumber" % "libphonenumber"                 % "7.0.+"

    val protobuf        = "com.google.protobuf"           %  "protobuf-java"                 % "2.6.1"

    val scodecBits      = "org.scodec"                    %% "scodec-bits"                   % "1.0.5"
    val scodecCore      = "org.scodec"                    %% "scodec-core"                   % "1.7.0"

    val scalazCore      = "org.scalaz"                    %% "scalaz-core"                   % V.scalaz
    val scalazConcurrent = "org.scalaz"                   %% "scalaz-concurrent"             % V.scalaz

    val shapeless       = "com.chuusai"                   %% "shapeless"                     % "2.1.0"

    val logbackClassic  = "ch.qos.logback"                % "logback-classic"                % "1.1.2"
    val scalaLogging    = "com.typesafe.scala-logging"    %% "scala-logging"                 % "3.1.0"

    val jodaTime        = "joda-time"                     %  "joda-time"                     % "2.7"
    val jodaConvert     = "org.joda"                      %  "joda-convert"                  % "1.7"

    val commonsCodec    = "commons-codec"                 % "commons-codec"                  % "1.10"
  }

  object Test {
    val akkaTestkit     = "com.typesafe.akka"             %% "akka-testkit"                  % V.akka % "test"
    val scalacheck      = "org.scalacheck"                %% "scalacheck"                    % "1.12.2" % "test"
    val specs2          = "org.specs2"                    %% "specs2-core"                   % "3.0" % "test"
    val slickTestkit    = "com.typesafe.slick"            %% "slick-testkit"                 % V.slick % "test"

    val jfairy          = "io.codearte.jfairy"            %  "jfairy"                        % "0.3.1" % "test"

    val utilTesting     = "im.actor"                      %% "actor-util-testing"            % "0.0.1-SNAPSHOT" % "test"
  }
  import Compile._, Test._

  val common = Seq(logbackClassic, scalaLogging)

  val root = common ++ Seq(
    akkaSlf4j, akkaActor, akkaKernel, akkaStream
  )

  val api = common ++ Seq(akkaSlf4j, akkaActor, akkaStream, commonsCodec)

  val rpcApi = common ++ Seq(
    akkaSlf4j, akkaActor, bcprov, libPhoneNumber, protobuf, shapeless
  )

  val internalServices = common ++ Seq(akkaActor, akkaStream, scodecBits)

  val session = common ++ Seq(
    akkaSlf4j, akkaActor, akkaKernel, akkaStream, scodecCore, scalazCore,
    akkaTestkit, specs2, utilTesting
  )

  val persist = common ++ Seq(postgresJdbc, slick, slickJoda, flywayCore, hikariCP, jodaTime, jodaConvert)

  val codecs = common ++ Seq(scalazCore, scodecBits, scodecCore)

  val models = common ++ Seq(scodecBits, scodecCore, sprayJson, jodaTime, jodaConvert)

  val frontend = common ++ Seq(
    akkaSlf4j, akkaActor, akkaKernel, akkaStream, akkaStreamWS,
    scodecBits, scodecCore,
    scalazCore, scalazConcurrent
  )

  val tests = common ++ Seq(akkaTestkit, jfairy, scalacheck, specs2, slickTestkit, utilTesting)
}
