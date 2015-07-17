resolvers ++= Seq(
  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
  "Sonatype OSS Releases" at "https://oss.sonatype.org/content/repositories/releases/",
  "Flyway" at "http://flywaydb.org/repo",
  Classpaths.sbtPluginReleases
)

addSbtPlugin("com.typesafe.akka" % "akka-sbt-plugin" % "2.2.3")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.7.5")

addSbtPlugin("io.spray" % "sbt-revolver" % "0.7.2")

addSbtPlugin("org.flywaydb" % "flyway-sbt" % "3.1")

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.0.4")

//addSbtPlugin("com.sksamuel.scapegoat" %% "sbt-scapegoat" % "0.94.6")

addSbtPlugin("im.actor" %% "sbt-actor-api" % "0.6.6")

//addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.3.9")

addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.4.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.0.3")

addSbtPlugin("com.trueaccord.scalapb" % "sbt-scalapb" % "0.5.9")