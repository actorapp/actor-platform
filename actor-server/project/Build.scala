package im.actor

import akka.sbt.AkkaKernelPlugin
import akka.sbt.AkkaKernelPlugin.{ Dist, distBootClass, distJvmOptions, outputDirectory }
import sbt.Keys._
import sbt._
import spray.revolver.RevolverPlugin._
import com.trueaccord.scalapb.{ScalaPbPlugin => PB}

object Build extends sbt.Build {
  val Version = "1.0.2038"
  val ScalaVersion = "2.11.7"

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
      PB.protobufSettings ++ Seq(
        //PB.javaConversions in PB.protobufConfig := true,
        libraryDependencies += "com.trueaccord.scalapb" %% "scalapb-runtime" % "0.5.9" % PB.protobufConfig
      ) ++
      Seq(
        initialize ~= { _ =>
          if (sys.props("java.specification.version") != "1.8")
            sys.error("Java 8 is required for this project.")
        },
        resolvers ++= Resolvers.seq,
        scalacOptions in Compile ++= defaultScalacOptions,
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
    .dependsOn(actorRunner)
    .aggregate(
      actorCommonsApi,
      actorCommonsBase,
//      actorDashboard,
      actorEmail,
      actorEnrich,
      actorFrontend,
      actorHttpApi,
      actorLlectro,
      actorModels,
      actorNotifications,
      actorPersist,
      actorPresences,
      actorSession,
      actorRpcApi,
      actorTests,
      actorUtils,
      actorUtilsCache,
      actorUtilsHttp
    )

  lazy val actorRunner = Project(
    id = "actor-runner",
    base = file("actor-runner"),
    settings = defaultSettings
  ).dependsOn(
      actorActivation,
      actorCommonsBase,
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
  ).dependsOn(actorEmail, actorSms)

  lazy val actorCommonsApi = Project(
    id = "actor-commons-api",
    base = file("actor-commons-api"),
    settings =
      defaultSettings ++
        SbtActorApi.settings ++
        Seq(
          libraryDependencies ++= Dependencies.commonsApi,
          scalacOptions in Compile := (scalacOptions in Compile).value.filterNot(_ == "-Ywarn-unused-import")
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
  ).dependsOn(actorRpcApi, actorUtils)

  lazy val actorHttpApi = Project(
    id = "actor-http-api",
    base = file("actor-http-api"),
    settings = defaultSettings ++ Seq(
      libraryDependencies ++= Dependencies.httpApi
    )
  ).dependsOn(actorPeerManagers, actorPersist, actorTls)

  lazy val actorLlectro = Project(
    id = "actor-llectro",
    base = file("actor-llectro"),
    settings = defaultSettings ++
      Seq(
        libraryDependencies ++= Dependencies.llectro
      )
  ).dependsOn(actorPersist)

  lazy val actorOAuth = Project(
    id = "actor-oauth",
    base = file("actor-oauth"),
    settings = defaultSettings ++
      Seq(
        libraryDependencies ++= Dependencies.oauth
      )
  ).dependsOn(actorPersist)

  lazy val actorPeerManagers = Project(
    id = "actor-peer-managers",
    base = file("actor-peer-managers"),
    settings = defaultSettings ++ Seq(
      libraryDependencies ++= Dependencies.peerManagers
    )
  ).dependsOn(actorModels, actorPush, actorSocial, actorUtils)

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
  ).dependsOn(actorCodecs, actorCommonsApi, actorCommonsBase, actorPersist, actorPresences, actorSessionMessages, actorUtils)

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
      actorCommonsApi,
      actorLlectro,
      actorHttpApi,//TODO: remove this dependency
      actorOAuth,
      actorPeerManagers,
      actorPersist,
      actorPresences,
      actorPush,
      actorSessionMessages,
      actorSms,
      actorSocial,
      actorUtils,
      actorUtilsCache,
      actorUtilsHttp,
      actorVoximplant)

  lazy val actorSms = Project(
    id = "actor-sms",
    base = file("actor-sms"),
    settings = defaultSettings ++ Seq(libraryDependencies ++= Dependencies.sms)
  ).dependsOn(actorUtils)

  lazy val actorSocial = Project(
    id = "actor-social",
    base = file("actor-social"),
    settings = defaultSettings ++ Seq(
      libraryDependencies ++= Dependencies.social
    )
  ).dependsOn(actorPersist)

  lazy val actorTls = Project(
    id = "actor-tls",
    base = file("actor-tls"),
    settings = defaultSettings ++ Seq(
      libraryDependencies ++= Dependencies.tls
    )
  )

  lazy val actorFrontend = Project(
    id = "actor-frontend",
    base = file("actor-frontend"),
    settings = defaultSettings ++ Seq(
      libraryDependencies ++= Dependencies.frontend
    )
  ).dependsOn(actorCommonsApi, actorSessionMessages, actorSession, actorTls)

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

  lazy val actorNotifications = Project(
    id = "actor-notifications",
    base = file("actor-notifications"),
    settings = defaultSettings ++ Seq(
      libraryDependencies ++= Dependencies.notifications
    )
  )
    .dependsOn(actorModels, actorPersist, actorSms, actorUtils)

  lazy val actorUtils = Project(
    id = "actor-utils",
    base = file("actor-utils"),
    settings = defaultSettings ++ Seq(
      libraryDependencies ++= Dependencies.utils
    )
  )
    .dependsOn(actorCommonsApi, actorModels, actorPersist)

  lazy val actorUtilsCache = Project(
    id = "actor-utils-cache",
    base = file("actor-utils-cache"),
    settings = defaultSettings ++ Seq(
      libraryDependencies ++= Dependencies.utilsCache
    )
  )

  lazy val actorUtilsHttp = Project(
    id = "actor-utils-http",
    base = file("actor-utils-http"),
    settings = defaultSettings ++ Seq(
      libraryDependencies ++= Dependencies.utilsHttp
    )
  )

  lazy val actorVoximplant = Project(
    id = "actor-voximplant",
    base = file("actor-voximplant"),
    settings = defaultSettings ++ Seq(
      libraryDependencies ++= Dependencies.voximplant
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
      actorActivation,
      actorCodecs,
      actorCommonsApi,
      actorCommonsBase,
//      actorDashboard,
      actorEmail,
      actorEnrich,
      actorFrontend,
      actorHttpApi,
      actorNotifications,
      actorOAuth,
      actorPersist,
      actorPush,
      actorRpcApi,
      actorRunner,
      actorSession
    )
}
