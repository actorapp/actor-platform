import dsl._
import Keys._

enablePlugins(JavaAppPackaging)

dockerBaseImage := "prettynatty/sbt"

dockerExposedPorts := Seq(8080)

packageName in Docker := "actorim-server"

version in Docker := version.value
