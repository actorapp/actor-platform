package im.actor.server

import java.io.File

import akka.persistence.kafka.server.TestServer
import org.scalatest.Suite

object KafkaSpec {
  private var kafkaServer: Option[TestServer] = None

  private def newServer(): Unit = {
    kafkaServer foreach (_.stop())
    cleanData()
    kafkaServer = Some(new TestServer())
  }

  private def cleanData(): Unit = {
    val dataDirFile = new File("data")
    if (dataDirFile.exists()) {
      org.apache.commons.io.FileUtils.deleteDirectory(dataDirFile)
    }
  }
}

trait KafkaSpec {
  this: Suite ⇒

  KafkaSpec.newServer()
}