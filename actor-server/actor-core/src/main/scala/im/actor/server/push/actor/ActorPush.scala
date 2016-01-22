package im.actor.server.push.actor

import akka.actor._
import akka.event.Logging
import com.rabbitmq.client.{ Consumer, TopologyRecoveryException, Channel, Connection }
import com.rabbitmq.client.impl.DefaultExceptionHandler
import com.spingo.op_rabbit._
import im.actor.server.model.push.ActorPushCredentials
import io.circe.generic.auto._
import io.circe.syntax._

import scala.util.{ Failure, Success, Try }

final case class ActorPushMessage(data: Map[String, String])

final class ActorPush(system: ActorSystem) extends Extension {
  private val log = Logging(system, getClass)

  val rabbitControl =
    Try(ConnectionParams.fromConfig(system.settings.config.getConfig("services.rabbitmq"))) match {
      case Success(config) ⇒
        system.actorOf(Props(new RabbitControl(config.copy(exceptionHandler = new ExceptionHandler(system)))), "rabbit-control")
      case Failure(e) ⇒
        log.error(e, "Failed to parse rabbitmq configuration")
        system.deadLetters
    }

  def deliver(seq: Int, creds: ActorPushCredentials): Unit =
    rabbitControl ! Message.topic(
      ActorPushMessage(data = Map(
        "seq" → seq.toString
      )).asJson.toString(),
      routingKey = creds.topic
    )
}

private final class ExceptionHandler(system: ActorSystem) extends DefaultExceptionHandler {
  private val log = Logging(system, getClass)

  override def handleUnexpectedConnectionDriverException(conn: Connection, exception: Throwable): Unit = {
    log.error(exception, "Unexpected connection driver")
    super.handleUnexpectedConnectionDriverException(conn, exception)
  }

  override def handleConsumerException(channel: Channel, exception: Throwable, consumer: Consumer, consumerTag: String, methodName: String): Unit = {
    log.error(exception, "Consumer exception, consumer: {}, consumerTag: {}, methodName: {}", consumer, consumerTag, methodName)
    super.handleConsumerException(channel, exception, consumer, consumerTag, methodName)
  }

  override def handleBlockedListenerException(connection: Connection, exception: Throwable): Unit = {
    log.error(exception, "Blocked listener")
    super.handleBlockedListenerException(connection, exception)
  }

  override def handleChannelRecoveryException(ch: Channel, exception: Throwable): Unit = {
    log.error(exception, "Channel recovery error")
    super.handleChannelRecoveryException(ch, exception)
  }

  override def handleFlowListenerException(channel: Channel, exception: Throwable): Unit = {
    log.error(exception, "Flow listener error")
    super.handleFlowListenerException(channel, exception)
  }

  override def handleChannelKiller(channel: Channel, exception: Throwable, what: String): Unit = {
    log.error(exception, "Channel killer, what: {}", what)
    super.handleChannelKiller(channel, exception, what)
  }

  override def handleReturnListenerException(channel: Channel, exception: Throwable): Unit = {
    log.error(exception, "Return listener error")
    super.handleReturnListenerException(channel, exception)
  }

  override def handleConnectionKiller(connection: Connection, exception: Throwable, what: String): Unit = {
    log.error(exception, "Connection killer, what: {}", what)
    super.handleConnectionKiller(connection, exception, what)
  }

  override def handleTopologyRecoveryException(conn: Connection, ch: Channel, exception: TopologyRecoveryException): Unit = {
    log.error(exception, "Topology recovery error, channel: {}", ch)
    super.handleTopologyRecoveryException(conn, ch, exception)
  }

  override def handleConfirmListenerException(channel: Channel, exception: Throwable): Unit = {
    log.error(exception, "Confirm listener error")
    super.handleConfirmListenerException(channel, exception)
  }

  override def handleConnectionRecoveryException(conn: Connection, exception: Throwable): Unit = {
    log.error(exception, "Connection recovery error")
    super.handleConnectionRecoveryException(conn, exception)
  }
}

object ActorPush extends ExtensionId[ActorPush] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): ActorPush = new ActorPush(system)

  override def lookup(): ExtensionId[_ <: Extension] = ActorPush
}