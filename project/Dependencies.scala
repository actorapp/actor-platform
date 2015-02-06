import sbt._

object Dependencies {
  object V {
    val akka = "2.3.9"
    val akkaExperimental = "1.0-M2"
    val scalaz = "7.1.0"
  }

  object Compile {
    val akkaActor       = "com.typesafe.akka"             %% "akka-actor"                    % V.akka
    val akkaKernel      = "com.typesafe.akka"             %% "akka-kernel"                   % V.akka
    val akkaStream      = "com.typesafe.akka"             %% "akka-stream-experimental"      % V.akkaExperimental
    val akkaHttp        = "com.typesafe.akka"             %% "akka-http-experimental"        % V.akkaExperimental
    val akkaHttpCore    = "com.typesafe.akka"             %% "akka-http-core-experimental"   % V.akkaExperimental
    val akkaHttpSpray   = "com.typesafe.akka"             %% "akka-http-spray-json-experimental" % V.akkaExperimental
    val akkaSlf4j       = "com.typesafe.akka"             %% "akka-slf4j"                    % V.akka

    val logbackClassic  = "ch.qos.logback"                % "logback-classic"                % "1.1.2"
  }

  object Test {
  }

  import Compile._, Test._

  val common = Seq(logbackClassic)

  val root = common ++ Seq(akkaSlf4j, akkaActor, akkaKernel, akkaStream)
}
