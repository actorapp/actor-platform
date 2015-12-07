package im.actor

import xerial.sbt.Sonatype.SonatypeKeys._

object Sonatype {
  val sonatypeSettings = Seq(sonatypeProfileName := "im.actor")
}