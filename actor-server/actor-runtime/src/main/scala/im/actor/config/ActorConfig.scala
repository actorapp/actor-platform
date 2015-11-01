package im.actor.config

import java.io.File

import akka.actor.ActorSystem

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
      s"""
        |akka {
        |  actor {
        |    provider: "akka.cluster.ClusterActorRefProvider"
        |  }
        |
        |  extensions: ["im.actor.server.db.DbExtension", "im.actor.server.bot.BotExtension", "akka.cluster.client.ClusterClientReceptionist"] $${akka.extensions}
        |
        |  loggers = ["akka.event.slf4j.Slf4jLogger"]
        |
        |  logging-filter = "akka.event.slf4j.Slf4jLoggingFilter"
        |
        |  actor {
        |    serializers {
        |      actor = "im.actor.serialization.ActorSerializer"
        |    }
        |
        |    serialization-bindings {
        |      "com.trueaccord.scalapb.GeneratedMessage" = actor
        |    }
        |  }
        |}
      """.stripMargin
    )
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

  def systemName(implicit system: ActorSystem) = system.settings.config.getString("name")

  def baseUrl(implicit system: ActorSystem) = {
    val config = system.settings.config
    val scheme = config.getString("webapp.scheme")
    val host = config.getString("webapp.host")
    s"$scheme://$host"
  }
}
