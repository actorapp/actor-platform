package im.actor

import sbt._

object Resolvers {
  lazy val seq = Seq(
    DefaultMavenRepository,
    Resolver.sonatypeRepo("snapshots"),
    Resolver.sonatypeRepo("releases"),
    Resolver.bintrayRepo("scalaz", "releases"),
    Resolver.bintrayRepo("dnvriend", "maven"),
    Resolver.bintrayRepo("dwhjames", "maven"),
    Resolver.bintrayRepo("krasserm", "maven"),
    "eaio" at "http://eaio.com/maven2",
    "actor snapshots" at "http://repos.81port.com/nexus/content/repositories/snapshots",
    "actor releases" at "http://repos.81port.com/nexus/content/repositories/releases",
    "gcm-server-repository" at "https://raw.githubusercontent.com/slorber/gcm-server-repository/master/releases/",
    "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/"
  )
}
