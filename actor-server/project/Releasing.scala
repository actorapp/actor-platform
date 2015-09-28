package im.actor

import bintray._
import sbt._
import Keys._
import com.typesafe.sbt.packager.debian.DebianPlugin.autoImport.Debian

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

  val releaseSettings = Seq(taskSetting)

  private def getCreds: BintrayCredentials = {
    (for {
      user <- sys.env.get("BINTRAY_USER")
      key <- sys.env.get("BINTRAY_API_KEY")
    } yield BintrayCredentials(user, key)) getOrElse {
      throw new RuntimeException("BINTRAY_USER or BINTRAY_API_KEY is not defined")
    }
  }
}