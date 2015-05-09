package im.actor.server

import java.io.File

import akka.persistence.kafka.server.TestServer
import org.scalatest.Suite

object KafkaSpec {
  cleanData()

  private var kafkaServer: TestServer = _

  newServer()

  private def newServer(): Unit = {
    if (Option(kafkaServer) == None) {
      kafkaServer = new TestServer()
    }
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

  protected def cleanKafka(): Unit = {
    KafkaSpec.kafkaServer.stop()
    KafkaSpec.cleanData()
    KafkaSpec.newServer
  }
}