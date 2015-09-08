package im.actor.server

import akka.actor._
import akka.testkit._
import com.typesafe.config._
import im.actor.config.ActorConfig
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures

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

abstract class ActorSuite(system: ActorSystem = { ActorSpecification.createSystem() })
  extends TestKit(system)
  with Suite
  with FlatSpecLike
  with BeforeAndAfterAll
  with Matchers
  with ScalaFutures {
  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
    system.awaitTermination()
  }
}
