package im.actor

import bintray._
import com.typesafe.sbt.pgp.PgpKeys
import sbt._
import Keys._
import xerial.sbt.Sonatype.SonatypeKeys
import xerial.sbt.Sonatype.autoImport._
import sbtrelease._
import ReleaseStateTransformations._
import ReleasePlugin.autoImport._

trait Releasing {
  private val publishDeb = taskKey[Unit]("Publish to debian repository")

  /*
  private lazy val taskSetting = publishDeb := {
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
*/

  lazy val releaseSettings = Seq(
    //taskSetting,
    releaseCommitMessage := s"chore(server): setting version to ${(version in ThisBuild).value}",
    releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
      runClean,
      setReleaseVersion,
      commitReleaseVersion,
      ReleaseStep(
        action = { state =>
          val extracted = Project extract state
          extracted.runAggregated(PgpKeys.publishSigned in Global in extracted.get(thisProjectRef), state)
        },
        enableCrossBuild = true
      ),
      setNextVersion,
      commitNextVersion,
      ReleaseStep(action = Command.process("sonatypeReleaseAll", _)),
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