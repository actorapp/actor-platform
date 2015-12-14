import dsl._
import Keys._

name := "actor"

enablePlugins(JavaServerAppPackaging)
enablePlugins(JDebPackaging)

name := "actor"

maintainer := "Actor LLC <oss@actor.im>"
packageSummary := "Messaging platform server"
packageDescription := "Open source messaging platform for team communications"
version in Debian := version.value
debianPackageDependencies in Debian ++= Seq("java8-runtime-headless")

rpmVendor := "actor"

daemonUser in Linux := "actor"
daemonGroup in Linux := (daemonUser in Linux).value

bashScriptExtraDefines += """addJava "-Dactor.home=${app_home}/..""""
bashScriptExtraDefines += """addJava "-Dlogback.configurationFile=${app_home}/../conf/logback.xml""""
bashScriptExtraDefines += """addJava -javaagent:${app_home}/../lib/aspectj-weaver.jar"""

dockerExposedPorts := Seq(9070, 9080, 9090)
packageName in Docker := "server"
version in Docker := version.value
dockerRepository := Some("actor")
dockerUpdateLatest := true
