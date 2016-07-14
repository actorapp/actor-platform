package im.actor

import sbt.Keys._
import sbt._
import spray.revolver.RevolverPlugin._
import com.trueaccord.scalapb.{ScalaPbPlugin => PB}
import com.typesafe.sbt.SbtMultiJvm
import com.typesafe.sbt.SbtMultiJvm.MultiJvmKeys.MultiJvm
import com.typesafe.sbt.packager.archetypes.JavaServerAppPackaging
import com.typesafe.sbt.packager.debian.JDebPackaging

object Build extends sbt.Build with Versioning with Releasing with Packaging {
  val ScalaVersion = "2.11.8"
  val BotKitVersion = getVersion

  lazy val buildSettings =
    Defaults.coreDefaultSettings ++
      Seq(
        //version := Version,
        scalaVersion := ScalaVersion,
        scalaVersion in ThisBuild := ScalaVersion,
        crossPaths := false,
        organization := "im.actor.server",
        organizationHomepage := Some(url("https://actor.im")),
        resolvers ++= Resolvers.seq,
        scalacOptions ++= Seq("-Yopt-warnings"),
        parallelExecution := true
      ) ++ Sonatype.sonatypeSettings

  lazy val pomExtraXml =
    <url>https://actor.im</url>
      <scm>
        <connection>scm:git:github.com/actorapp/actor-platform.git</connection>
        <developerConnection>scm:git:git@github.com:actorapp/actor-platform.git</developerConnection>
        <url>github.com/(your repository url)</url>
      </scm>
      <developers>
        <developer>
          <id>prettynatty</id>
          <name>Andrey Kuznetsov</name>
          <url>https://github.com/prettynatty</url>
        </developer>
        <developer>
          <id>rockjam</id>
          <name>Nikolay Tatarinov</name>
          <url>https://github.com/rockjam</url>
        </developer>
      </developers>

  lazy val defaultSettingsBotkit =
    buildSettings ++
    ActorHouseRules.actorDefaultSettings(
      "im.actor",
      ActorHouseRules.PublishType.PublishToSonatype,
      pomExtraXml
    )

  lazy val defaultSettingsServer =
      buildSettings ++
        ActorHouseRules.actorDefaultSettings(
          "im.actor.server",
          ActorHouseRules.PublishType.PublishToSonatype,
          pomExtraXml) ++
      PB.protobufSettings ++ Seq(
      PB.singleLineToString in PB.protobufConfig := true,
      libraryDependencies += "com.trueaccord.scalapb" %% "scalapb-runtime" % "0.5.32" % PB.protobufConfig,
      dependencyOverrides ~= { overrides =>
        overrides + "com.google.protobuf" % "protobuf-java" % "3.0.0-beta-2"
      },
      PB.includePaths in PB.protobufConfig ++= Seq(
        file("actor-models/src/main/protobuf"),
        file("actor-core/src/main/protobuf"),
        file("actor-fs-adapters/src/main/protobuf")
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
        fork in Test := false,
        updateOptions := updateOptions.value.withCachedResolution(true)
      )

  lazy val root = Project(
    "actor",
    file("."),
    settings =
      packagingSettings ++
      defaultSettingsServer ++
      Revolver.settings ++
      Seq(
        libraryDependencies ++= Dependencies.root,
        //Revolver.reStartArgs := Seq("im.actor.server.Main"),
        mainClass in Revolver.reStart := Some("im.actor.server.Main"),
        mainClass in Compile := Some("im.actor.server.Main"),
        autoCompilerPlugins := true,
        scalacOptions in(Compile, doc) ++= Seq(
          "-Ywarn-unused-import",
          "-groups",
          "-implicits",
          "-diagrams"
        )
      )
  )
    .settings(net.virtualvoid.sbt.graph.Plugin.graphSettings: _*)
    .settings(releaseSettings)
    .dependsOn(actorServerSdk)
    .aggregate(
      actorServerSdk,
      actorTestkit,
      actorTests
    )
    .settings(aggregate in Revolver.reStart := false)
    .enablePlugins(JavaServerAppPackaging, JDebPackaging)

  lazy val actorActivation = Project(
    id = "actor-activation",
    base = file("actor-activation"),
    settings = defaultSettingsServer ++
      Seq(
        libraryDependencies ++= Dependencies.activation,
        scalacOptions in Compile := (scalacOptions in Compile).value.filterNot(_ == "-Ywarn-unused-import")
      )
  )
    .dependsOn(actorCore, actorEmail, actorSms, actorPersist)

  lazy val actorBots = Project(
    id = "actor-bots",
    base = file("actor-bots"),
    settings = defaultSettingsServer ++
      Seq(libraryDependencies ++= Dependencies.bots)
  )
    .dependsOn(actorCore, actorHttpApi, actorTestkit % "test")

  lazy val actorBotsShared = Project(
    id = "actor-bots-shared",
    base = file("actor-bots-shared"),
    settings = defaultSettingsBotkit ++ Seq(
      version := BotKitVersion,
      libraryDependencies <+= (scalaVersion)("org.scala-lang" % "scala-reflect" % _),
      libraryDependencies ++= Dependencies.botShared,
      addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
    )
  )

  lazy val actorBotkit = Project(
    id = "actor-botkit",
    base = file("actor-botkit"),
    settings = defaultSettingsBotkit ++ Revolver.settings ++ Seq(
      version := BotKitVersion,
      libraryDependencies ++= Dependencies.botkit,
      addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
    )
  )
    .dependsOn(actorBotsShared)
    .aggregate(actorBotsShared)

  lazy val actorCli = Project(
    id = "actor-cli",
    base = file("actor-cli"),
    settings = defaultSettingsServer ++ Revolver.settings ++ Seq(
      libraryDependencies ++= Dependencies.cli,
      mainClass in Revolver.reStart := Some("im.actor.server.cli.ActorCliApp"),
      mainClass in Compile := Some("im.actor.server.cli.ActorCliApp")
    )
  )
    .dependsOn(actorCore, actorFrontend)

  lazy val actorCore = Project(
    id = "actor-core",
    base = file("actor-core"),
    settings = defaultSettingsServer ++ SbtActorApi.settings ++ Seq(
      libraryDependencies ++= Dependencies.core,
      scalacOptions in Compile := (scalacOptions in Compile).value.filterNot(_ == "-Xfatal-warnings")
    )
  )
    .dependsOn(actorCodecs, actorFileAdapter, actorModels, actorPersist, actorRuntime)

  lazy val actorEmail = Project(
    id = "actor-email",
    base = file("actor-email"),
    settings = defaultSettingsServer ++
      Seq(
        libraryDependencies ++= Dependencies.email
      )
  )
    .dependsOn(actorRuntime)

  lazy val actorEnrich = Project(
    id = "actor-enrich",
    base = file("actor-enrich"),
    settings = defaultSettingsServer ++ Seq(
      libraryDependencies ++= Dependencies.enrich,
      scalacOptions in Compile := (scalacOptions in Compile).value.filterNot(_ == "-Xfatal-warnings")
    )
  )
    .dependsOn(actorRpcApi, actorRuntime)

  lazy val actorHttpApi = Project(
    id = "actor-http-api",
    base = file("actor-http-api"),
    settings = defaultSettingsServer ++ Seq(
      libraryDependencies ++= Dependencies.httpApi
    )
  )
    .dependsOn(actorPersist, actorRuntime)//runtime deps because of ActorConfig

  lazy val actorNotify = Project(
    id = "actor-notify",
    base = file("actor-notify"),
    settings = defaultSettingsServer ++
      Seq(libraryDependencies ++= Dependencies.shared)
  )
    .dependsOn(actorCore, actorEmail)

  lazy val actorOAuth = Project(
    id = "actor-oauth",
    base = file("actor-oauth"),
    settings = defaultSettingsServer ++
      Seq(
        libraryDependencies ++= Dependencies.oauth
      )
  )
    .dependsOn(actorPersist)

  lazy val actorSession = Project(
    id = "actor-session",
    base = file("actor-session"),
    settings = defaultSettingsServer ++ Seq(
      libraryDependencies ++= Dependencies.session
    )
  )
    .dependsOn(actorPersist, actorCore, actorCodecs, actorCore, actorRpcApi)

  lazy val actorSessionMessages = Project(
    id = "actor-session-messages",
    base = file("actor-session-messages"),
    settings = defaultSettingsServer ++ Seq(libraryDependencies ++= Dependencies.sessionMessages)
  )
    .dependsOn(actorCore)

  lazy val actorRpcApi = Project(
    id = "actor-rpc-api",
    base = file("actor-rpc-api"),
    settings = defaultSettingsServer ++ Seq(
      libraryDependencies ++= Dependencies.rpcApi,
      scalacOptions in Compile := (scalacOptions in Compile).value.filterNot(_ == "-Xfatal-warnings")
    )
  )
    .dependsOn(
    actorActivation,
    actorCore,
    actorOAuth,
    actorSessionMessages,
    actorSms)

  lazy val actorSms = Project(
    id = "actor-sms",
    base = file("actor-sms"),
    settings = defaultSettingsServer ++ Seq(libraryDependencies ++= Dependencies.sms)
  )
    .dependsOn(actorRuntime)

  lazy val actorFileAdapter = Project(
    id = "actor-fs-adapters",
    base = file("actor-fs-adapters"),
    settings = defaultSettingsServer ++ Seq(
      libraryDependencies ++= Dependencies.fileAdapter,
      scalacOptions in Compile := (scalacOptions in Compile).value.filterNot(_ == "-Xfatal-warnings")
    )
  )
    .dependsOn(actorHttpApi, actorPersist)

  lazy val actorFrontend = Project(
    id = "actor-frontend",
    base = file("actor-frontend"),
    settings = defaultSettingsServer ++ Seq(
      libraryDependencies ++= Dependencies.frontend,
      scalacOptions in Compile := (scalacOptions in Compile).value.filterNot(_ == "-Xfatal-warnings")
    )
  )
    .dependsOn(actorCore, actorSession)

  lazy val actorCodecs = Project(
    id = "actor-codecs",
    base = file("actor-codecs"),
    settings = defaultSettingsServer ++ Seq(
      libraryDependencies ++= Dependencies.codecs
    )
  )
    .dependsOn(actorModels)

  lazy val actorModels = Project(
    id = "actor-models",
    base = file("actor-models"),
    settings = defaultSettingsServer ++ Seq(
      libraryDependencies ++= Dependencies.models
    )
  )

  lazy val actorPersist = Project(
    id = "actor-persist",
    base = file("actor-persist"),
    settings = defaultSettingsServer ++ Seq(
      libraryDependencies ++= Dependencies.persist
    )
  )
    .dependsOn(actorModels, actorRuntime)

  lazy val actorTestkit = Project(
    id = "actor-testkit",
    base = file("actor-testkit"),
    settings = defaultSettingsServer ++ Seq(
      libraryDependencies ++= Dependencies.tests
    )
  ).configs(Configs.all: _*)
    .dependsOn(
      actorCore,
      actorRpcApi,
      actorSession
    )

  lazy val actorRuntime = Project(
    id = "actor-runtime",
    base = file("actor-runtime"),
    settings = defaultSettingsServer ++ Seq(
      libraryDependencies ++= Dependencies.runtime
    )
  )

  lazy val actorServerSdk = Project(
    id = "actor-server-sdk",
    base = file("actor-server-sdk"),
    settings = defaultSettingsServer ++ Seq(
      libraryDependencies ++= Dependencies.sdk
    )
  )
    .dependsOn(
    actorActivation,
    actorBots,
    actorCli,
    actorEnrich,
    actorEmail,
    actorFrontend,
    actorHttpApi,
    actorNotify,
    actorOAuth,
    actorRpcApi
  ).aggregate(
    actorActivation,
    actorBots,
    actorCli,
    actorCodecs,
    actorCore,
    actorEmail,
    actorEnrich,
    actorFileAdapter,
    actorFrontend,
    actorHttpApi,
    actorModels,
    actorNotify,
    actorOAuth,
    actorPersist,
    actorRpcApi,
    actorRuntime,
    actorSession,
    actorSessionMessages,
    actorSms
  )

  lazy val actorTests = Project(
    id = "actor-tests",
    base = file("actor-tests"),
    settings = defaultSettingsServer ++ Testing.settings ++ Seq(
      libraryDependencies ++= Dependencies.tests,
      compile in MultiJvm <<= (compile in MultiJvm) triggeredBy (compile in Test),
      scalacOptions in Compile := (scalacOptions in Compile).value.filterNot(_ == "-Xfatal-warnings"),
      executeTests in Test <<= (executeTests in Test, executeTests in MultiJvm) map {
        case (testResults, multiNodeResults)  =>
          val overall =
            if (testResults.overall.id < multiNodeResults.overall.id)
              multiNodeResults.overall
            else
              testResults.overall
          Tests.Output(overall,
            testResults.events ++ multiNodeResults.events,
            testResults.summaries ++ multiNodeResults.summaries)
      }
    ))
    .configs(Configs.all: _*)
      .configs(MultiJvm)
    .dependsOn(
      actorTestkit % "test",
      actorActivation,
      actorBots,
      actorCodecs,
      actorCore,
      actorEmail,
      actorEnrich,
      actorFrontend,
      actorHttpApi,
      actorOAuth,
      actorPersist,
      actorRpcApi,
      actorSession
    )
}
