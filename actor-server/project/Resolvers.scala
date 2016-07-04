package im.actor

import sbt._

object Resolvers {
  lazy val seq = Seq(
    DefaultMavenRepository,
    Resolver.sonatypeRepo("releases"),
    Resolver.bintrayRepo("dnvriend", "maven"),
    Resolver.bintrayRepo("dwhjames", "maven"),
    Resolver.bintrayRepo("krasserm", "maven"),

    // FIXME: remove after slick/slick#1274 released
    Resolver.bintrayRepo("kwark", "maven"),

    // for op-rabbit
    "The New Motion Public Repo" at "http://nexus.thenewmotion.com/content/groups/public/",
    // for akka-rabbitmq (needed by op-rabbit)
    "SpinGo OSS" at "http://spingo-oss.s3.amazonaws.com/repositories/releases",

    Resolver.sonatypeRepo("snapshots")
  )
}
