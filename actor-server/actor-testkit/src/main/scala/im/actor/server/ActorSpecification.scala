package im.actor.server

import akka.actor._
import akka.stream.ActorMaterializer
import akka.testkit._
import com.typesafe.config._
import im.actor.config.ActorConfig
import kamon.Kamon
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object ActorSpecification {
  private[this] def defaultSystemName = "actor-server-test"

  def createSystem(systemName: String = defaultSystemName): ActorSystem = {
    createSystem(systemName, createConfig(systemName, ConfigFactory.empty()))
  }

  def createSystem(config: Config): ActorSystem = {
    createSystem(defaultSystemName, createConfig(defaultSystemName, config))
  }

  def createSystem(systemName: String, config: Config): ActorSystem = {
    ActorSystem(systemName, config)
  }

  def createConfig(systemName: String, initialConfig: Config): Config = {
    val maxPort = 65535
    val minPort = 1025
    val port = scala.util.Random.nextInt(maxPort - minPort + 1) + minPort

    val host = "127.0.0.1"

    initialConfig
      .withFallback(
        ConfigFactory.parseString(s"""
          akka.remote.netty.tcp.port = $port
          akka.remote.netty.tcp.hostname = "$host"
          akka.cluster.seed-nodes = [ "akka.tcp://$systemName@$host:$port" ]
        """)
      )
      .withFallback(ActorConfig.load())
  }
}

abstract class ActorSuite(system: ActorSystem = { Kamon.start(); ActorSpecification.createSystem() })
  extends TestKit(system)
  with Suite
  with FlatSpecLike
  with BeforeAndAfterAll
  with Matchers
  with ScalaFutures {
  protected implicit val mat = ActorMaterializer()(system)

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
    Await.result(system.whenTerminated, Duration.Inf)
  }
}
