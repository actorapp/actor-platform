package im.actor.util

import java.nio.file.Files

import im.actor.config.ActorConfig
import org.scalatest.{ FlatSpec, Matchers }

class PathSpec extends FlatSpec with Matchers {

  "Templates folder" should "exist" in templatesFolderExists()

  def templatesFolderExists(): Unit = {
    val path = ActorConfig.getAbsolutePath("templates")
    assert(Files.exists(path), "templates path should exist")
    assert(Files.isDirectory(path), "templates is a folder")
    assert(path.isAbsolute, "templates path is absolute")
  }

}