import sbt._

object Dependencies {
  object V {
    val akka = "2.3.9"
    val akkaExperimental = "1.0-M3"
    val scalaz = "7.1.1"
  }

  object Compile {
    val akkaActor       = "com.typesafe.akka"             %% "akka-actor"                    % V.akka
    val akkaKernel      = "com.typesafe.akka"             %% "akka-kernel"                   % V.akka
    val akkaStream      = "com.typesafe.akka"             %% "akka-stream-experimental"      % V.akkaExperimental
    val akkaHttp        = "com.typesafe.akka"             %% "akka-http-experimental"        % V.akkaExperimental
    val akkaHttpCore    = "com.typesafe.akka"             %% "akka-http-core-experimental"   % V.akkaExperimental
    val akkaHttpSpray   = "com.typesafe.akka"             %% "akka-http-spray-json-experimental" % V.akkaExperimental
    val akkaSlf4j       = "com.typesafe.akka"             %% "akka-slf4j"                    % V.akka

    val scodecBits      = "org.typelevel"                 %% "scodec-bits"                   % "1.0.4"
    val scodecCore      = "org.typelevel"                 %% "scodec-core"                   % "1.6.0"

    val scalazCore      = "org.scalaz"                    %% "scalaz-core"                   % V.scalaz
    val scalazConcurrent = "org.scalaz"                   %% "scalaz-concurrent"             % V.scalaz

    val logbackClassic  = "ch.qos.logback"                % "logback-classic"                % "1.1.2"
    val scalaLogging    = "com.typesafe.scala-logging"    %% "scala-logging"                 % "3.1.0"
  }

  object Test {
    val akkaTestkit     = "com.typesafe.akka"             %% "akka-testkit"                  % V.akka
    val scalacheck      = "org.scalacheck"                %% "scalacheck"                    % "1.12.2" % "test"
    val specs2          = "org.specs2"                    %% "specs2-core"                   % "2.4.15"
  }

  import Compile._, Test._

  val common = Seq(logbackClassic, scalaLogging)

  val tests = Seq(akkaTestkit, scalacheck, specs2)

  val root = common ++ Seq(akkaSlf4j, akkaActor, akkaKernel, akkaStream, scodecBits, scodecCore, scalazCore, scalazConcurrent)
}
