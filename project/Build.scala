package im.actor

import akka.sbt.AkkaKernelPlugin
import akka.sbt.AkkaKernelPlugin.{Dist, distBootClass, distJvmOptions, outputDirectory}
import sbt.Keys._
import sbt._
import spray.revolver.RevolverPlugin._

object Build extends sbt.Build {
  val Organization = "Actor IM"
  val Version = "0.1.0-SNAPSHOT"
  val ScalaVersion = "2.11.6"

  lazy val buildSettings =
    Defaults.coreDefaultSettings ++
      Seq(
        organization := Organization,
        version := Version,
        scalaVersion := ScalaVersion,
        crossPaths := false,
        organizationName := Organization,
        organizationHomepage := Some(url("https://actor.im"))
      )

  lazy val compilerWarnings = Seq(
    "-Ywarn-dead-code",
    "-Ywarn-infer-any",
    "-Ywarn-numeric-widen",
    "-Ywarn-unused-import"
  )

  lazy val defaultScalacOptions = Seq(
    "-target:jvm-1.8",
    "-encoding",
    "UTF-8",
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
      Seq(
        initialize ~= { _ =>
          if (sys.props("java.specification.version") != "1.8")
            sys.error("Java 8 is required for this project.")
        },
        resolvers ++= Resolvers.seq,
        scalacOptions in Compile ++= defaultScalacOptions,
        scalacOptions in (Compile, console) ~= (_.filterNot(_ == "-Ywarn-unused-import").filterNot(_ == "-Yfatal-warnings")),
        javaOptions ++= Seq("-Dfile.encoding=UTF-8", "-Dscalac.patmat.analysisBudget=off"),
        javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint:unchecked", "-Xlint:deprecation")
      )

  lazy val root = Project(
    "actor-server",
    file("."),
    settings =
      defaultSettings ++
        AkkaKernelPlugin.distSettings ++
        Revolver.settings ++
        Seq(
          libraryDependencies ++= Dependencies.root,
          distJvmOptions in Dist := "-server -Xms256M -Xmx1024M",
          distBootClass in Dist := "im.actor.server.Main",
          outputDirectory in Dist := file("target/dist"),
          Revolver.reStartArgs := Seq("im.actor.server.Main"),
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
    .dependsOn(actorFrontend, actorCommonsBase, actorRpcApi)
    .aggregate(actorCommonsApi, actorCommonsBase, actorFrontend, actorModels, actorPersist, actorPresences, actorSession, actorRpcApi, actorTests)

  lazy val actorCommonsApi = Project(
    id = "actor-commons-api",
    base = file("actor-commons-api"),
    settings =
      defaultSettings ++
        SbtActorApi.settings ++
        Seq(
          libraryDependencies ++= Dependencies.commonsApi
        )
  ).dependsOn(actorPersist, actorCodecs)

  lazy val actorCommonsBase = Project(
    id = "actor-commons-base",
    base = file("actor-commons-base"),
    settings =
      defaultSettings ++
        Seq(
          libraryDependencies ++= Dependencies.commonsBase
        )
  )

  lazy val actorSession = Project(
    id = "actor-session",
    base = file("actor-session"),
    settings = defaultSettings ++ Seq(
      libraryDependencies ++= Dependencies.session
    )
  ).dependsOn(actorPersist, actorCodecs, actorCommonsApi, actorRpcApi, actorPush)

  lazy val actorSessionMessages = Project(
    id = "actor-session-messages",
    base = file("actor-session-messages"),
    settings = defaultSettings ++ Seq(libraryDependencies ++= Dependencies.sessionMessages)
  ).dependsOn(actorCommonsApi)

  lazy val actorPush = Project(
    id = "actor-push",
    base = file("actor-push"),
    settings = defaultSettings ++ Seq(
      libraryDependencies ++= Dependencies.push
    )
  ).dependsOn(actorCodecs, actorCommonsApi, actorCommonsBase, actorPersist, actorPresences, actorSessionMessages)

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
  ).dependsOn(actorCodecs, actorCommonsApi, actorPersist, actorPresences, actorPush, actorSessionMessages, actorSms, actorSocial)

  lazy val actorSms = Project(
    id = "actor-sms",
    base = file("actor-sms"),
    settings = defaultSettings ++ Seq(libraryDependencies ++= Dependencies.sms)
  )

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
  ).dependsOn(actorCommonsApi, actorSessionMessages, actorSession)

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

  lazy val actorTests = Project(
    id = "actor-tests",
    base = file("actor-tests"),
    settings = defaultSettings ++ Testing.settings ++ Seq(
      libraryDependencies ++= Dependencies.tests
    ))
    .configs(Configs.all: _*)
    .dependsOn(actorCodecs, actorCommonsApi, actorCommonsBase, actorFrontend, actorPersist, actorPush, actorRpcApi, actorSession)
}
