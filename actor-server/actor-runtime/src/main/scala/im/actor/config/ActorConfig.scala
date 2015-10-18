package im.actor.config

import java.io.File

import scala.collection.JavaConversions._
import scala.concurrent.duration._
import java.util.concurrent.TimeUnit

import com.typesafe.config.{ ConfigException, Config, ConfigFactory }

import scala.util.{ Failure, Success, Try }

object ActorConfig {
  def load(): Config = {
    val mainConfig = Option(System.getProperty("actor.home")) match {
      case Some(home) ⇒
        ConfigFactory.load(ConfigFactory.parseFile(new File(s"$home/conf/server.conf")))
      case None ⇒ ConfigFactory.load()
    }

    val config = ConfigFactory.parseString(
      """
        |akka {
        |  actor {
        |    provider: "akka.cluster.ClusterActorRefProvider"
        |  }
        |
        |  extensions: ["im.actor.server.db.DbExtension", "im.actor.server.bot.BotExtension", "akka.cluster.client.ClusterClientReceptionist"]
        |}
        |
        |jdbc-connection {
        |  jndiPath: "/"
        |  dataSourceName: "DefaultDataSource"
        |}
      """.stripMargin
    )
      .withFallback(ConfigFactory.parseResources("runtime.conf"))
      .withFallback(mainConfig)
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
