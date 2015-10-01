package im.actor

import sbt.Keys._
import sbt._
import xerial.sbt.Sonatype.autoImport._

trait Publishing {
  val publishSettings = Seq(
    organization := "im.actor",
  
    sonatypeProfileName := "org.xerial",

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
