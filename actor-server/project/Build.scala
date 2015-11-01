package im.actor

import sbt.Keys._
import sbt._
import spray.revolver.RevolverPlugin._
import com.trueaccord.scalapb.{ScalaPbPlugin => PB}
import com.typesafe.sbt.packager.docker.DockerPlugin.autoImport.Docker

object Build extends sbt.Build with Versioning with Releasing with Publishing {
  val ScalaVersion = "2.11.7"
  val Version = getVersion

  lazy val buildSettings =
    Defaults.coreDefaultSettings ++
      Seq(
        version := Version,
        scalaVersion := ScalaVersion,
        crossPaths := false,
        organizationHomepage := Some(url("https://actor.im"))
      )

  lazy val compilerWarnings = Seq(
    "-Ywarn-dead-code",
    "-Ywarn-infer-any",
    "-Ywarn-numeric-widen"
  )

  lazy val defaultScalacOptions = Seq(
    "-target:jvm-1.8",
    "-Ybackend:GenBCode",
    "-Ydelambdafy:method",
    "-Yopt:l:classpath",
    //"-Ymacro-debug-lite",
    "-encoding", "UTF-8",
    "-deprecation",
    "-unchecked",
    "-feature",
    "-language:higherKinds",
    "-Xfatal-warnings",
    "-Xlint",
    "-Xfuture"
  ) ++ compilerWarnings

  lazy val defaultSettings =
    buildSettings ++ Formatting.formatSettings ++
      PB.protobufSettings ++ Seq(
      //PB.javaConversions in PB.protobufConfig := true,
      libraryDependencies += "com.trueaccord.scalapb" %% "scalapb-runtime" % "0.5.9" % PB.protobufConfig,
      PB.includePaths in PB.protobufConfig ++= Seq(
        file("actor-core/src/main/protobuf")
      ),
      PB.runProtoc in PB.protobufConfig := (args =>
        com.github.os72.protocjar.Protoc.runProtoc("-v300" +: args.toArray))
    ) ++
      Seq(
        initialize ~= { _ =>
          if (sys.props("java.specification.version") != "1.8")
            sys.error("Java 8 is required for this project.")
        },
        resolvers ++= Resolvers.seq,
        scalacOptions in Compile ++= defaultScalacOptions,
        javaOptions ++= Seq("-Dfile.encoding=UTF-8"),
        javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint:unchecked", "-Xlint:deprecation"),
        fork in Test := false,
        updateOptions := updateOptions.value.withCachedResolution(true)
      )


  lazy val root = Project(
    "actor",
    file("."),
    settings =
      defaultSettings ++ releaseSettings ++
        Revolver.settings ++
        Seq(
          libraryDependencies ++= Dependencies.root,
          //Revolver.reStartArgs := Seq("im.actor.server.Main"),
          mainClass in Revolver.reStart := Some("im.actor.server.Main"),
          mainClass in Compile := Some("im.actor.server.Main"),
          autoCompilerPlugins := true,
          scalacOptions in(Compile, doc) ++= Seq(
            "-groups",
            "-implicits",
            "-diagrams"
          )
        )
  ).settings(net.virtualvoid.sbt.graph.Plugin.graphSettings: _*)
    .dependsOn(actorRunner, actorCli)
    .aggregate(
      //      actorDashboard,
      actorBots,
      actorBotsShared,
      actorBotkit,
      actorRuntime,
      actorTests
    )
    .settings(
    aggregate in Docker := false,
    aggregate in Revolver.reStart := false
  )

  lazy val actorRunner = Project(
    id = "actor-runner",
    base = file("actor-runner"),
    settings = defaultSettings ++ Seq(
      libraryDependencies ++= Dependencies.runner
    )
  ).dependsOn(
    actorActivation,
    actorBots,
    actorCli,
    actorEnrich,
    actorEmail,
    actorFrontend,
    actorHttpApi,
    actorRpcApi,
    actorNotifications,
    actorOAuth
  )

  lazy val actorActivation = Project(
    id = "actor-activation",
    base = file("actor-activation"),
    settings = defaultSettings ++
      Seq(
        libraryDependencies ++= Dependencies.activation,
        scalacOptions in Compile := (scalacOptions in Compile).value.filterNot(_ == "-Ywarn-unused-import")
      )
  ).dependsOn(actorEmail, actorSms, actorPersist)

  lazy val actorBots = Project(
    id = "actor-bots",
    base = file("actor-bots"),
    settings = defaultSettings ++
      Seq(libraryDependencies ++= Dependencies.bots)
  )
    .dependsOn(actorBotkit, actorCore, actorTestkit % "test")

  lazy val actorBotsShared = Project(
    id = "actor-bots-shared",
    base = file("actor-bots-shared"),
    settings = defaultSettings ++ publishSettings ++ Seq(
      libraryDependencies <+= (scalaVersion)("org.scala-lang" % "scala-reflect" % _),
      libraryDependencies ++= Dependencies.botShared,
      addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0-M5" cross CrossVersion.full)
    )
  )

  lazy val actorBotkit = Project(
    id = "actor-botkit",
    base = file("actor-botkit"),
    settings = defaultSettings ++ publishSettings ++ Revolver.settings ++ Seq(
      libraryDependencies ++= Dependencies.botkit,
      addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0-M5" cross CrossVersion.full)
    )
  )
    .dependsOn(actorBotsShared)
    .aggregate(actorBotsShared)

  lazy val actorCli = Project(
    id = "actor-cli",
    base = file("actor-cli"),
    settings = defaultSettings ++ Revolver.settings ++ Seq(
      libraryDependencies ++= Dependencies.cli,
      mainClass in Revolver.reStart := Some("im.actor.server.cli.ActorCliApp"),
      mainClass in Compile := Some("im.actor.server.cli.ActorCliApp")
    )
  )

  lazy val actorCore = Project(
    id = "actor-core",
    base = file("actor-core"),
    settings = defaultSettings ++ SbtActorApi.settings ++ Seq(
      libraryDependencies ++= Dependencies.core
    )
  ).dependsOn(actorCodecs, actorModels, actorPersist, actorPresences, actorSocial, actorRuntime)

  lazy val actorEmail = Project(
    id = "actor-email",
    base = file("actor-email"),
    settings = defaultSettings ++
      Seq(
        libraryDependencies ++= Dependencies.email
      )
  )

  lazy val actorEnrich = Project(
    id = "actor-enrich",
    base = file("actor-enrich"),
    settings = defaultSettings ++ Seq(
      libraryDependencies ++= Dependencies.enrich
    )
  ).dependsOn(actorRpcApi, actorRuntime)

  lazy val actorHttpApi = Project(
    id = "actor-http-api",
    base = file("actor-http-api"),
    settings = defaultSettings ++ Seq(
      libraryDependencies ++= Dependencies.httpApi
    )
  ).dependsOn(actorBots, actorCore, actorPersist, actorRuntime)

  lazy val actorOAuth = Project(
    id = "actor-oauth",
    base = file("actor-oauth"),
    settings = defaultSettings ++
      Seq(
        libraryDependencies ++= Dependencies.oauth
      )
  ).dependsOn(actorPersist)

  lazy val actorSession = Project(
    id = "actor-session",
    base = file("actor-session"),
    settings = defaultSettings ++ Seq(
      libraryDependencies ++= Dependencies.session
    )
  ).dependsOn(actorPersist, actorCore, actorCodecs, actorCore, actorRpcApi)

  lazy val actorSessionMessages = Project(
    id = "actor-session-messages",
    base = file("actor-session-messages"),
    settings = defaultSettings ++ Seq(libraryDependencies ++= Dependencies.sessionMessages)
  ).dependsOn(actorCore)

  lazy val actorPresences = Project(
    id = "actor-presences",
    base = file("actor-presences"),
    settings = defaultSettings ++ Seq(libraryDependencies ++= Dependencies.presences)
  ).dependsOn(actorPersist)

  lazy val actorRpcApi = Project(
    id = "actor-rpc-api",
    base = file("actor-rpc-api"),
    settings = defaultSettings ++ Seq(
      libraryDependencies ++= Dependencies.rpcApi
    )
  ).dependsOn(
    actorActivation,
    actorCodecs,
    actorCore,
    actorOAuth,
    actorPersist,
    actorPresences,
    actorSessionMessages,
    actorSms,
    actorSocial,
    actorRuntime)

  lazy val actorSms = Project(
    id = "actor-sms",
    base = file("actor-sms"),
    settings = defaultSettings ++ Seq(libraryDependencies ++= Dependencies.sms)
  ).dependsOn(actorRuntime)

  lazy val actorSocial = Project(
    id = "actor-social",
    base = file("actor-social"),
    settings = defaultSettings ++ Seq(
      libraryDependencies ++= Dependencies.social
    )
  ).dependsOn(actorPersist)

  lazy val actorFrontend = Project(
    id = "actor-frontend",
    base = file("actor-frontend"),
    settings = defaultSettings ++ Seq(
      libraryDependencies ++= Dependencies.frontend
    )
  ).dependsOn(actorCore, actorSessionMessages, actorSession)

  lazy val actorCodecs = Project(
    id = "actor-codecs",
    base = file("actor-codecs"),
    settings = defaultSettings ++ Seq(
      libraryDependencies ++= Dependencies.codecs
    )
  ).dependsOn(actorModels)

  lazy val actorModels = Project(
    id = "actor-models",
    base = file("actor-models"),
    settings = defaultSettings ++ Seq(
      libraryDependencies ++= Dependencies.models
    )
  )

  lazy val actorPersist = Project(
    id = "actor-persist",
    base = file("actor-persist"),
    settings = defaultSettings ++ Seq(
      libraryDependencies ++= Dependencies.persist
    )
  ).dependsOn(actorModels)

  //  lazy val actorDashboard = Project(
  //    id = "actor-dashboard",
  //    base = file("actor-dashboard"),
  //    settings = defaultSettings ++ Seq(
  //      scalacOptions in Compile := (scalacOptions in Compile).value.filterNot(_ == "-Ywarn-unused-import"),
  //      javaOptions := javaOptions.value.filterNot(_.startsWith("-Dscalac.patmat.analysisBudget")),
  //      libraryDependencies ++= Dependencies.dashboard
  //    )
  //  )
  //    .enablePlugins(PlayScala)
  //    .dependsOn(actorPersist, actorUtils)

  lazy val actorTestkit = Project(
    id = "actor-testkit",
    base = file("actor-testkit"),
    settings = defaultSettings ++ Seq(
      libraryDependencies ++= Dependencies.tests
    )
  ).configs(Configs.all: _*)
    .dependsOn(
      actorCore,
      actorRpcApi,
      actorSession,
      actorPresences
    )

  lazy val actorNotifications = Project(
    id = "actor-notifications",
    base = file("actor-notifications"),
    settings = defaultSettings ++ Seq(
      libraryDependencies ++= Dependencies.notifications
    )
  )
    .dependsOn(actorCore, actorModels, actorPersist, actorSms, actorRuntime)

  lazy val actorRuntime = Project(
    id = "actor-runtime",
    base = file("actor-runtime"),
    settings = defaultSettings ++ Seq(
      libraryDependencies ++= Dependencies.runtime
    )
  )

  lazy val actorTests = Project(
    id = "actor-tests",
    base = file("actor-tests"),
    settings = defaultSettings ++ Testing.settings ++ Seq(
      libraryDependencies ++= Dependencies.tests
    ))
    .configs(Configs.all: _*)
    .dependsOn(
      actorTestkit % "test",
      actorActivation,
      actorCodecs,
      actorCore,
      //      actorDashboard,
      actorEmail,
      actorEnrich,
      actorFrontend,
      actorHttpApi,
      actorNotifications,
      actorOAuth,
      actorPersist,
      actorRpcApi,
      actorSession
    )
}
