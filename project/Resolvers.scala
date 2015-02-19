import sbt._

object Resolvers {
  lazy val seq = Seq(
    DefaultMavenRepository,
    Resolver.typesafeRepo("releases"),
    Resolver.sonatypeRepo("snapshots"),
    Resolver.sonatypeRepo("releases"),
    "actor snapshots" at "http://repos.81port.com/nexus/content/repositories/snapshots",
    "actor releases" at "http://repos.81port.com/nexus/content/repositories/releases"
  )
}
