package im.actor.config

import java.nio.file.{ Path, Paths }
import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import com.typesafe.config.{ Config, ConfigException, ConfigFactory }

import scala.collection.JavaConversions._
import scala.concurrent.duration._
import scala.util.{ Failure, Success, Try }

object ActorConfig {

  def load(defaults: Config = ConfigFactory.empty()): Config = {
    val mainConfig = ConfigFactory.load()

    val config = defaults.withFallback(ConfigFactory.parseString(
      s"""
        |akka {
        |  actor {
        |    provider: "akka.cluster.ClusterActorRefProvider"
        |  }
        |
        |  extensions: [
        |    "im.actor.server.db.DbExtension",
        |    "akka.cluster.client.ClusterClientReceptionist",
        |    "im.actor.server.push.actor.ActorPush"
        |  ] $${akka.extensions}
        |
        |  loggers = ["akka.event.slf4j.Slf4jLogger"]
        |  logging-filter = "akka.event.slf4j.Slf4jLoggingFilter"
        |
        |  cluster.sharding.state-store-mode = "ddata"
        |
        |  persistence {
        |    journal.plugin: "jdbc-journal"
        |    snapshot-store.plugin: "jdbc-snapshot-store"
        |  }
        |
        |  stream {
        |    materializer {
        |      auto-fusing: off
        |    }
        |  }
        |}
        |
        |akka-persistence-jdbc {
        |  tables {
        |    journal {
        |      tableName = "persistence_journal"
        |    }
        |    deletedTo {
        |      tableName = "persistence_deleted_to"
        |    }
        |    snapshot {
        |      tableName = "persistence_snapshot"
        |    }
        |  }
        |}
      """.stripMargin
    ))
      .withFallback(mainConfig)
      .withFallback(ConfigFactory.parseResources("runtime.conf"))
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

  def projectName(implicit system: ActorSystem) = system.settings.config.getString("project-name")

  def baseUrl(implicit system: ActorSystem) = {
    val config = system.settings.config
    config.getString("http.base-uri")
  }
}
