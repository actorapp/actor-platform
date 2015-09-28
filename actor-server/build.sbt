import dsl._
import Keys._

enablePlugins(JavaServerAppPackaging)
enablePlugins(JDebPackaging)
enablePlugins(RpmPlugin)

JavaAppPackaging.projectSettings
JavaServerAppPackaging.debianSettings

name := "actor"

maintainer := "Actor LLC <oss@actor.im>"
packageSummary := "Messaging platform server"
packageDescription := "Open source messaging platform for team communications"
version in Debian := version.value

rpmVendor := "actor"

daemonUser in Linux := "actor"
daemonGroup in Linux := (daemonUser in Linux).value

bashScriptExtraDefines += """addJava "-Dconfig.file=${app_home}/../conf/server.conf""""
bashScriptExtraDefines += """addJava "-Dlogback.configurationFile=${app_home}/../conf/logback.xml""""

dockerExposedPorts := Seq(9070, 9080, 9090)
packageName in Docker := "server"
version in Docker := version.value
dockerRepository := Some("actor")
dockerUpdateLatest := true
