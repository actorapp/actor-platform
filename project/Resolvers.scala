import sbt._

object Resolvers {
  lazy val seq = Seq(
    "typesafe repo"       at "http://repo.typesafe.com/typesafe/releases",
    "sonatype snapshots"  at "https://oss.sonatype.org/content/repositories/snapshots/",
    "sonatype releases"   at "https://oss.sonatype.org/content/repositories/releases/"
  )
}
