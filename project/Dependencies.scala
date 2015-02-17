import sbt._

object Dependencies {
  object V {
    val akka = "2.3.9"
    val akkaExperimental = "1.0-M3"
    val scalaz = "7.1.1"
    val slick = "3.0.0-M1"
  }

  object Compile {
    val akkaActor       = "com.typesafe.akka"             %% "akka-actor"                    % V.akka
    val akkaKernel      = "com.typesafe.akka"             %% "akka-kernel"                   % V.akka
    val akkaStream      = "com.typesafe.akka"             %% "akka-stream-experimental"      % V.akkaExperimental
    val akkaHttp        = "com.typesafe.akka"             %% "akka-http-experimental"        % V.akkaExperimental
    val akkaHttpCore    = "com.typesafe.akka"             %% "akka-http-core-experimental"   % V.akkaExperimental
    val akkaHttpSpray   = "com.typesafe.akka"             %% "akka-http-spray-json-experimental" % V.akkaExperimental
    val akkaSlf4j       = "com.typesafe.akka"             %% "akka-slf4j"                    % V.akka

    val postgresJdbc    = "org.postgresql"                %  "postgresql"                    % "9.4-1200-jdbc41" exclude("org.slf4j", "slf4j-simple")
    val slick           = "com.typesafe.slick"            %% "slick"                         % V.slick
    val slickJoda       = "com.github.tototoshi"          %% "slick-joda-mapper"             % "1.2.0"
    val flywayCore      = "org.flywaydb"                  %  "flyway-core"                   % "3.1"
    val hikariCP        = "com.zaxxer"                    %  "HikariCP"                      % "2.3.2"

    val scodecBits      = "org.typelevel"                 %% "scodec-bits"                   % "1.0.4"
    val scodecCore      = "org.typelevel"                 %% "scodec-core"                   % "1.6.0"

    val scalazCore      = "org.scalaz"                    %% "scalaz-core"                   % V.scalaz
    val scalazConcurrent = "org.scalaz"                   %% "scalaz-concurrent"             % V.scalaz

    val logbackClassic  = "ch.qos.logback"                % "logback-classic"                % "1.1.2"
    val scalaLogging    = "com.typesafe.scala-logging"    %% "scala-logging"                 % "3.1.0"

    val jodaTime        = "joda-time"                     %  "joda-time"                     % "2.7"
    val jodaConvert     = "org.joda"                      %  "joda-convert"                  % "1.7"
  }

  object Test {
    val akkaTestkit     = "com.typesafe.akka"             %% "akka-testkit"                  % V.akka % "test"
    val scalacheck      = "org.scalacheck"                %% "scalacheck"                    % "1.12.2" % "test"
    val specs2          = "org.specs2"                    %% "specs2-core"                   % "2.4.15" % "test"
    val slickTestkit    = "com.typesafe.slick"            %% "slick-testkit"                 % V.slick % "test"
  }

  import Compile._, Test._

  val common = Seq(logbackClassic, scalaLogging, jodaTime, jodaConvert)

  val tests = common ++ Seq(akkaTestkit, scalacheck, specs2, slickTestkit)

  val persist = common ++ Seq(postgresJdbc, slick, slickJoda, flywayCore, hikariCP)

  val root = common ++ Seq(akkaSlf4j, akkaActor, akkaKernel, akkaStream, scodecBits, scodecCore, scalazCore, scalazConcurrent)
}
