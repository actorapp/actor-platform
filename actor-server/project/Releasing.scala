package im.actor

import bintray._
import com.typesafe.sbt.packager.debian.DebianPlugin
import DebianPlugin.autoImport._
import com.typesafe.sbt.packager.universal.UniversalPlugin
import UniversalPlugin.autoImport._
import com.typesafe.sbt.pgp.PgpKeys
import sbt._
import Keys._
import sbtrelease._
import ReleaseStateTransformations._
import ReleasePlugin.autoImport._

trait Releasing {
  private val publishDeb = taskKey[Unit]("Publish to debian repository")

  private val taskSetting = publishDeb := {
    val btyOrg = "actor"
    val repoName = "ubuntu"
    val pkgName = "actor"
    val vers = version.value
    val f = (packageBin in Debian).value
    val path = "pool/main/a/" + name.value + "/" + f.getName + ";deb_distribution=trusty;deb_component=main;deb_architecture=all"
    val log = streams.value.log

    val creds = getCreds

    val repo = BintrayRepo(creds, Some(btyOrg), repoName)
    repo.upload(pkgName, vers, path, f, log)
    repo.release(pkgName, vers, log)
  }

  val releaseSettings = Seq(
    taskSetting,
    releaseCommitMessage := s"chore(server): setting version to ${(version in ThisBuild).value}",
    releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
      // runClean, FIXME: uncomment after fixing stalled clean compile
      setReleaseVersion,
      commitReleaseVersion,
      ReleaseStep(
        action = { state =>
          val extracted = Project extract state
          extracted.runAggregated(PgpKeys.publishSigned in Global in extracted.get(thisProjectRef), state)
        },
        enableCrossBuild = true
      ),
      ReleaseStep(
        action = { state =>
          Command.process("sonatypeReleaseAll", state)
        }
      ),
      ReleaseStep(
        action = { state =>
          val extracted = Project extract state
          extracted.runTask(publishDeb in Global in extracted.get(thisProjectRef), state)._1
        }
      ),
      ReleaseStep(
        action = { state =>
          val extracted = Project extract state
          extracted.runTask(packageBin in Debian in extracted.get(thisProjectRef), state)._1
        }
      ),
      ReleaseStep(
        action = { state =>
          val extracted = Project extract state
          extracted.runTask(dist in Universal in extracted.get(thisProjectRef), state)._1
        }
      ),
      setNextVersion,
      commitNextVersion,
      pushChanges
    )
  )

  private def getCreds: BintrayCredentials = {
    (for {
      user <- sys.env.get("BINTRAY_USER")
      key <- sys.env.get("BINTRAY_API_KEY")
    } yield BintrayCredentials(user, key)) getOrElse {
      throw new RuntimeException("BINTRAY_USER or BINTRAY_API_KEY is not defined")
    }
  }
}