import sbt._

object Resolvers {
  lazy val seq = Seq(
    "typesafe repo"       at "http://repo.typesafe.com/typesafe/releases",
    "sonatype snapshots"  at "https://oss.sonatype.org/content/repositories/snapshots/",
    "sonatype releases"   at "https://oss.sonatype.org/content/repositories/releases/",
    "actor snapshots" at "http://repos.81port.com/nexus/content/repositories/snapshots",
    "actor releases" at "http://repos.81port.com/nexus/content/repositories/releases"
  )
}
