package im.actor

import sbt._

object Configs {
  val IntegrationTest = config("it") extend Runtime
  val EndToEndTest = config("e2e") extend Runtime
  val all = Seq(IntegrationTest, EndToEndTest)
}