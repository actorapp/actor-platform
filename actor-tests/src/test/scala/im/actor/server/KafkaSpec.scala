package im.actor.server

import java.io.File

import akka.persistence.kafka.server.TestServer
import org.scalatest.{ BeforeAndAfterAll, Suite }

object KafkaSpec {
  private var kafkaServer: Option[TestServer] = None

  private def newServer(): Unit = {
    stopServer()
    cleanData()
    kafkaServer = Some(new TestServer())
  }

  private def stopServer(): Unit = {
    kafkaServer foreach (_.stop())
  }

  private def cleanData(): Unit = {
    val dataDirFile = new File("data")
    if (dataDirFile.exists()) {
      org.apache.commons.io.FileUtils.deleteDirectory(dataDirFile)
    }
  }
}

trait KafkaSpec extends BeforeAndAfterAll {
  this: Suite ⇒

  import KafkaSpec._

  KafkaSpec.newServer()

  override def afterAll: Unit = {
    super.afterAll()
    stopServer()
    cleanData()
  }
}