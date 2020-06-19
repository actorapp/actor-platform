resolvers ++= Seq(
  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
  "Sonatype OSS Releases" at "https://oss.sonatype.org/content/repositories/releases/",
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
  Resolver.url("actor-sbt-plugins", url("https://dl.bintray.com/actor/sbt-plugins"))(Resolver.ivyStylePatterns),
  "Flyway" at "http://flywaydb.org/repo",
  Classpaths.sbtPluginReleases,
  "Github-API" at "http://repo.jenkins-ci.org/public/"
)

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.7.5")

addSbtPlugin("io.spray" % "sbt-revolver" % "0.7.2")

addSbtPlugin("im.actor" %% "sbt-actor-api" % "0.7.18")

addSbtPlugin("com.trueaccord.scalapb" % "sbt-scalapb" % "0.5.32")

addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.0.0")

addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "0.5.1")

addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.0.6")

// This version is no longer available in any resolver, so the jar has to be added as an unmanaged dependency
addSbtPlugin("ohnosequences" % "sbt-github-release" % "0.3.0" from "file:///./lib/sbt-github-release-0.3.0.jar")

addSbtPlugin("com.typesafe.sbt" % "sbt-aspectj" % "0.10.0")

addSbtPlugin("im.actor" % "actor-sbt-houserules" % "0.1.9")

addSbtPlugin("com.typesafe.sbt" % "sbt-multi-jvm" % "0.3.8")

libraryDependencies ++= Seq(
  "com.github.os72" % "protoc-jar" % "3.0.0-b3",
  "org.kohsuke" % "github-api" % "1.77"
)
