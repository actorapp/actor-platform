import sbt._
import sbt.Keys._

object Testing {
  lazy val testAll = TaskKey[Unit]("test-all")

  private object Configs {
    val IntegrationTest = config("it") extend Runtime
    val EndToEndTest = config("e2e") extend Runtime
    val all = Seq(IntegrationTest, EndToEndTest)
  }

  private lazy val itSettings =
    inConfig(IntegrationTest)(Defaults.testSettings) ++
      Seq(
        fork in IntegrationTest := false,
        parallelExecution in IntegrationTest := false,
        scalaSource in IntegrationTest := baseDirectory.value / "src/it/scala")

  private lazy val e2eSettings =
    inConfig(Configs.EndToEndTest)(Defaults.testSettings) ++
      Seq(
        fork in Configs.EndToEndTest := false,
        parallelExecution in Configs.EndToEndTest := false,
        scalaSource in Configs.EndToEndTest := baseDirectory.value / "src/e2e/scala")

  lazy val settings = itSettings ++ e2eSettings ++ Seq(
    testAll <<= (test in Configs.EndToEndTest).dependsOn((test in IntegrationTest).dependsOn(test in Test))
  )
}