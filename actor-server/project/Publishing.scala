package im.actor

import sbt.Keys._
import sbt._

trait Publishing {
  val publishSettings = Seq(
    organization := "im.actor",

    publishTo := {
      val nexus = "https://oss.sonatype.org/"
      if (isSnapshot.value)
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases"  at nexus + "service/local/staging/deploy/maven2")
    },

    // To sync with Maven central, you need to supply the following information:
    pomExtra in Global :=
      <url>https://actor.im</url>
        <licenses>
          <license>
            <name>MIT</name>
            <url>http://www.opensource.org/licenses/MIT</url>
          </license>
        </licenses>
        <scm>
          <connection>scm:git:github.com/actorapp/actor-platform.git</connection>
          <developerConnection>scm:git:git@github.com:actorapp/actor-platform.git</developerConnection>
          <url>github.com/(your repository url)</url>
        </scm>
        <developers>
          <developer>
            <id>prettynatty</id>
            <name>Andrey Kuznetsov</name>
            <url>https://github.com/prettynatty</url>
          </developer>
        </developers>
  )
}
