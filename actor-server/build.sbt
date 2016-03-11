import dsl._
import Keys._

name := "actor"

enablePlugins(JavaServerAppPackaging)
enablePlugins(JDebPackaging)

scriptClasspath := Seq("*")

maintainer := "Actor LLC <oss@actor.im>"
packageSummary := "Messaging platform server"
packageDescription := "Open source messaging platform for team communications"
version in Debian := version.value
debianPackageDependencies in Debian ++= Seq("java8-runtime-headless")

rpmVendor := "actor"

daemonUser in Linux := "actor"
daemonGroup in Linux := (daemonUser in Linux).value

bashScriptExtraDefines += """addJava "-Dlogback.configurationFile=${app_home}/../conf/logback.xml""""
bashScriptExtraDefines += """addJava -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${app_home}/../logs/dump-`date`.hprof"""

dockerExposedPorts := Seq(9070, 9080, 9090)
packageName in Docker := "server"
version in Docker := version.value
dockerRepository := Some("actor")
dockerUpdateLatest := true

linuxPackageMappings += {
  val initFiles = sourceDirectory.value / "linux" / "var" / "lib" / "actor"
  packageMapping(initFiles -> "/var/lib/actor") withPerms "0644" withUser "actor" withGroup "actor" withContents()
}

linuxPackageMappings += {
  packageMapping(baseDirectory.value / "templates" -> "/usr/share/actor/templates") withContents()
}