package im.actor

import sbt._

object Resolvers {
  lazy val seq = Seq(
    DefaultMavenRepository,
    Resolver.sonatypeRepo("releases"),
    Resolver.bintrayRepo("scalaz", "releases"),
    Resolver.bintrayRepo("dnvriend", "maven"),
    Resolver.bintrayRepo("dwhjames", "maven"),
    Resolver.bintrayRepo("krasserm", "maven"),
    "gcm-server-repository" at "https://raw.githubusercontent.com/slorber/gcm-server-repository/master/releases/",
    Resolver.sonatypeRepo("snapshots")
  )
}
