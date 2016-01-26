resolvers ++= Seq(
  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
  "Sonatype OSS Releases" at "https://oss.sonatype.org/content/repositories/releases/",
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
  Resolver.url("actor-sbt-plugins", url("https://dl.bintray.com/actor/sbt-plugins"))(Resolver.ivyStylePatterns),
  "Flyway" at "http://flywaydb.org/repo",
  "Era7 maven releases" at "https://s3-eu-west-1.amazonaws.com/releases.era7.com",
  Classpaths.sbtPluginReleases
)

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.7.5")

addSbtPlugin("io.spray" % "sbt-revolver" % "0.7.2")

addSbtPlugin("org.flywaydb" % "flyway-sbt" % "3.1")

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.3.3")

addSbtPlugin("im.actor" %% "sbt-actor-api" % "0.7.4")

addSbtPlugin("com.trueaccord.scalapb" % "sbt-scalapb" % "0.5.21")

addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.0.0")

addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "0.5.1")

addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.0.6")

addSbtPlugin("ohnosequences" % "sbt-github-release" % "0.3.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-aspectj" % "0.10.0")

addSbtPlugin("im.actor" % "actor-sbt-houserules" % "0.1.9")

addSbtPlugin("com.typesafe.sbt" % "sbt-multi-jvm" % "0.3.8")

libraryDependencies ++= Seq(
  "com.github.os72" % "protoc-jar" % "3.0.0-b2"
)