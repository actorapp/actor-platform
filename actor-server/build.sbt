import dsl._
import Keys._

enablePlugins(JavaServerAppPackaging)
enablePlugins(DebianPlugin)

name in Debian := "actor-server"
maintainer in Linux := "Andrey Kuznetsov <smith@actor.im>"
packageSummary in Linux := "An Actor.IM server"
packageDescription := "Fast and furious business communications server"
daemonUser in Linux := "actor"
daemonGroup in Linux := (daemonUser in Linux).value
bashScriptExtraDefines += """addJava "-Dconfig.file=${app_home}/../conf/application.conf""""
bashScriptConfigLocation := Some("${app_home}/../conf/jvmopts")
javaOptions in Universal ++= Seq("""-Dlogback.configurationFile=${app_home}/../conf/logback.xml""")

dockerExposedPorts := Seq(9070, 9080, 9090)
packageName in Docker := "actor-server"
version in Docker := version.value
