package im.actor

import com.typesafe.sbt.SbtNativePackager._
import com.typesafe.sbt.packager.Keys._
import com.typesafe.sbt.packager.linux.LinuxPlugin.autoImport.packageMapping
import sbt.Keys._
import sbt._

private[actor] trait Packaging {
  lazy val packagingSettings = Seq(
    scriptClasspath := Seq("*"),

    maintainer := "Actor LLC <oss@actor.im>",
    packageSummary := "Messaging platform server",
    packageDescription := "Open source messaging platform for team communications",
    version in Debian := version.value,
    debianPackageDependencies in Debian ++= Seq(
      "java8-runtime-headless",
      "libapr1",
      "openssl (>= 1.0.2)"
    ),
    daemonUser in Linux := "actor",
    daemonGroup in Linux := (daemonUser in Linux).value,

    bashScriptExtraDefines += """addJava "-Dlogback.configurationFile=${app_home}/../conf/logback.xml"""",
    bashScriptExtraDefines += """addJava -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${app_home}/../logs/dump-`date`.hprof""",

    linuxPackageMappings += {
      val initFiles = sourceDirectory.value / "linux" / "var" / "lib" / "actor"
      packageMapping(initFiles -> "/var/lib/actor") withPerms "0644" withUser "actor" withGroup "actor" withContents()
    },
    linuxPackageMappings += {
      packageMapping(baseDirectory.value / "templates" -> "/usr/share/actor/templates") withContents()
    }
  )

}
