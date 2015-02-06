resolvers ++= Seq(
  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
  "sonatype-releases" at "https://oss.sonatype.org/content/repositories/releases/",
  Resolver.url("secret repository", url("http://repos.81port.com/nexus/content/repositories/snapshots"))(Resolver.ivyStylePatterns),
  "Flyway" at "http://flywaydb.org/repo"
)

addSbtPlugin("com.typesafe.akka" % "akka-sbt-plugin" % "2.2.3")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.7.4")

addSbtPlugin("io.spray" % "sbt-revolver" % "0.7.2")
