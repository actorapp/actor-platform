import sbt._
import sbt.Keys._
import akka.sbt.AkkaKernelPlugin
import akka.sbt.AkkaKernelPlugin.{ Dist, outputDirectory, distJvmOptions, distBootClass }
import im.actor.SbtActorApi
import spray.revolver.RevolverPlugin._

object Build extends sbt.Build {
  val Organization = "Actor IM"
  val Version = "0.1-SNAPSHOT"
  val ScalaVersion = "2.11.5"

  lazy val buildSettings =
    Defaults.defaultSettings ++
      Seq(
        organization         := Organization,
        version              := Version,
        scalaVersion         := ScalaVersion,
        crossPaths           := false,
        organizationName     := Organization,
        organizationHomepage := Some(url("https://actor.im"))
      )

  lazy val compilerWarnings = Seq(
    "-Ywarn-dead-code",
    "-Ywarn-infer-any",
    "-Ywarn-numeric-widen",
    "-Ywarn-unused"
  )

  lazy val defaultSettings =
    buildSettings ++
      Seq(
        initialize ~= { _ =>
          if (sys.props("java.specification.version") != "1.8")
            sys.error("Java 8 is required for this project.")
        },
        resolvers                 ++= Resolvers.seq,
        scalacOptions in Compile  ++= Seq(
          "-encoding",
          "UTF-8",
          "-deprecation",
          "-unchecked",
          "-feature",
          "-language:higherKinds"
        ) ++ compilerWarnings,
        javaOptions               ++= Seq("-Dfile.encoding=UTF-8", "-Dscalac.patmat.analysisBudget=off"),
        javacOptions              ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint:unchecked", "-Xlint:deprecation"),
        parallelExecution in Test :=  false,
        fork              in Test :=  true
      )

  lazy val root = Project(
    "actor-server",
    file("."),
    settings =
      defaultSettings               ++
      AkkaKernelPlugin.distSettings ++
      Revolver.settings             ++
      Seq(
        libraryDependencies                       ++= Dependencies.root,
        distJvmOptions       in Dist              :=  "-server -Xms256M -Xmx1024M",
        distBootClass        in Dist              :=  "im.actor.server.ApiKernel",
        outputDirectory      in Dist              :=  file("target/dist"),
        Revolver.reStartArgs                      :=  Seq("im.actor.server.Main"),
        mainClass            in Revolver.reStart  :=  Some("im.actor.server.Main"),
        autoCompilerPlugins                       :=  true,
        scalacOptions        in (Compile,doc)     :=  Seq(
          "-groups",
          "-implicits",
          "-diagrams"
        ) ++ compilerWarnings
      )
  ).settings(net.virtualvoid.sbt.graph.Plugin.graphSettings: _*)
  .dependsOn(actorFrontend)
  .aggregate(actorApi, actorFrontend, actorModels, actorPersist, actorSession, actorRpcApi, actorTests)

  lazy val actorSession = Project(
    id = "actor-session",
    base = file("actor-session"),
    settings = defaultSettings ++ Seq(
      libraryDependencies ++= Dependencies.session
    )
  ).dependsOn(actorPersist, actorFrontend, actorCodecs, actorApi, actorRpcApi)

  lazy val actorApi = Project(
    id = "actor-api",
    base = file("actor-api"),
    settings = defaultSettings ++ Seq(
      libraryDependencies ++= Dependencies.api
    )
  ).dependsOn(actorPersist, actorCodecs)

  lazy val actorRpcApi = Project(
    id = "actor-rpc-api",
    base = file("actor-rpc-api"),
    settings = defaultSettings ++ SbtActorApi.settings ++ Seq(
      libraryDependencies ++= Dependencies.rpcApi
    )
  ).dependsOn(actorCodecs, actorPersist)

  lazy val actorFrontend = Project(
    id = "actor-frontend",
    base = file("actor-frontend"),
    settings = defaultSettings ++ Seq(
      libraryDependencies ++= Dependencies.frontend
    )
  ).dependsOn(actorApi)

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
    settings = defaultSettings ++ Seq(
      libraryDependencies ++= Dependencies.tests
    )
  ).dependsOn(actorApi, actorRpcApi, actorPersist, actorCodecs)
}
