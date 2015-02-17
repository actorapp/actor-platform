import sbt._
import sbt.Keys._
import akka.sbt.AkkaKernelPlugin
import akka.sbt.AkkaKernelPlugin.{ Dist, outputDirectory, distJvmOptions, distBootClass }
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

  lazy val compilerWarns = Seq(
    "-Ywarn-dead-code",
    "-Ywarn-infer-any",
    "-Ywarn-numeric-widen",
    "-Ywarn-unused",
    "-Ywarn-unused-import"
  )

  lazy val defaultSettings =
    buildSettings ++
      Seq(
        initialize ~= { _ =>
          if (sys.props("java.specification.version") != "1.8")
            sys.error("Java 8 is required for this project.")
        },
        resolvers                 ++= Resolvers.seq,
        scalacOptions             ++= Seq(
          "-encoding",
          "UTF-8",
          "-deprecation",
          "-unchecked",
          "-feature",
          "-language:higherKinds"
        ) ++ compilerWarns,
        javaOptions               ++= Seq("-Dfile.encoding=UTF-8"),
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
        ) ++ compilerWarns
      )
  ).settings(net.virtualvoid.sbt.graph.Plugin.graphSettings: _*)
  .dependsOn(actorApi)
  .aggregate(actorApi, actorModels, actorPersist, actorTests)

  lazy val actorApi = Project(
    id = "actor-api",
    base = file("actor-api"),
    settings = defaultSettings ++ Seq(
      libraryDependencies ++= Dependencies.root
    )
  ).dependsOn(actorPersist)

  lazy val actorTests = Project(
    id = "actor-tests",
    base = file("actor-tests"),
    settings = defaultSettings ++ Seq(
      libraryDependencies ++= Dependencies.tests
    )
  ).dependsOn(actorApi)

  lazy val actorModels = Project(
    id = "actor-models",
    base = file("actor-models"),
    settings = defaultSettings
  )

  lazy val actorPersist = Project(
    id = "actor-persist",
    base = file("actor-persist"),
    settings = defaultSettings ++ Seq(
      libraryDependencies ++= Dependencies.persist
    )
  ).dependsOn(actorModels)
}
