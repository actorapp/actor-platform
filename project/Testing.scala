package im.actor

import sbt._
import sbt.Keys._

object Testing {

  import BuildKeys._
  import Configs._

  private lazy val testSettings = Seq(
    fork in Test := false,
    parallelExecution in Test := false
  )

  private lazy val itSettings = inConfig(IntegrationTest)(Defaults.testSettings) ++ Seq(
    scalaSource in IntegrationTest := baseDirectory.value / "src/it/scala",
    resourceDirectory in IntegrationTest := baseDirectory.value / "src/it/resources"
  )

  private lazy val e2eSettings = inConfig(EndToEndTest)(Defaults.testSettings) ++ Seq(
    scalaSource in EndToEndTest := baseDirectory.value / "src/e2e/scala",
    resourceDirectory in EndToEndTest := baseDirectory.value / "src/e2e/resources",
    fork in EndToEndTest := false
  )

  lazy val settings = testSettings ++ itSettings ++ e2eSettings ++ Seq(
    testAll := (),
    testAll <<= testAll.dependsOn(test in EndToEndTest),
    testAll <<= testAll.dependsOn(test in IntegrationTest),
    testAll <<= testAll.dependsOn(test in Test)
  )
}