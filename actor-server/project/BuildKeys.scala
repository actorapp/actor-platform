package im.actor

import sbt._

object BuildKeys {
  lazy val testAll = TaskKey[Unit]("test-all")
}