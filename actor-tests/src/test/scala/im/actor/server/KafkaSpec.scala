package im.actor.server

import java.io.File

import akka.persistence.kafka.server.TestServer
import org.scalatest.{ Suite, BeforeAndAfterAll }

trait KafkaSpec extends BeforeAndAfterAll {
  this: Suite ⇒

  cleanData()

  private val kafkaServer = new TestServer()

  override def afterAll: Unit = {
    super.afterAll()
    kafkaServer.stop()
  }

  private def cleanData(): Unit = {
    val dataDirFile = new File("data")
    if (dataDirFile.exists()) {
      org.apache.commons.io.FileUtils.deleteDirectory(dataDirFile)
    }
  }
}