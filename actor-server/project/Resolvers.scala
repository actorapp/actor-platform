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
    "gcm-server-repository" at "https://raw.githubusercontent.com/slorber/gcm-server-repository/master/releases/",
    "hseeberger" at "http://dl.bintray.com/hseeberger/maven"
  )
}
