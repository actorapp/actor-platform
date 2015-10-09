package im.actor.config

import scala.collection.JavaConversions._
import scala.concurrent.duration._
import java.util.concurrent.TimeUnit

import com.typesafe.config.{ ConfigException, Config, ConfigFactory }

import scala.util.{ Failure, Success, Try }

object ActorConfig {
  def load(): Config = {
    val config = ConfigFactory.parseString(
      """
        |akka {
        |  actor {
        |    provider: "akka.cluster.ClusterActorRefProvider"
        |  }
        |}
        |
        |jdbc-connection {
        |  jndiPath: "/"
        |  dataSourceName: "DefaultDataSource"
        |}
      """.stripMargin
    )
      .withFallback(ConfigFactory.load())
      .withFallback(ConfigFactory.parseString(
        """
          |akka {
          |  persistence {
          |    journal.plugin: "jdbc-journal"
          |    snapshot-store.plugin: "jdbc-snapshot-store"
          |  }
          |}
        """.stripMargin
      ))
      .resolve()

    // Compatibility with old config which used "enabled-modules"
    Try(config.getConfig("enabled-modules")) match {
      case Success(oldModConfig) ⇒
        ConfigFactory.parseMap(Map("modules" → oldModConfig.root())).withFallback(config).resolve()
      case Failure(_: ConfigException.Missing) ⇒ config
      case Failure(e)                          ⇒ throw e
    }
  }

  val defaultTimeout: FiniteDuration = ActorConfig.load().getDuration("common.default-timeout", TimeUnit.MILLISECONDS).millis
}
