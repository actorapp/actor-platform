package im.actor.server.sequence

import java.io.File
import java.util.concurrent.{ ExecutionException, TimeUnit, TimeoutException }

import akka.actor.{ ActorSystem, ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider }
import akka.event.Logging
import com.relayrides.pushy.apns.ApnsClient
import com.relayrides.pushy.apns.util.SimpleApnsPushNotification
import im.actor.server.db.DbExtension
import im.actor.server.model.push.ApplePushCredentials
import im.actor.server.persist.push.ApplePushCredentialsRepo
import im.actor.util.log.AnyRefLogSource

import scala.collection.concurrent.TrieMap
import scala.concurrent.Future
import scala.concurrent.blocking
import scala.concurrent.duration._
import scala.util.Try

object ApplePushExtension extends ExtensionId[ApplePushExtension] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): ApplePushExtension = new ApplePushExtension(system)

  override def lookup(): ExtensionId[_ <: Extension] = ApplePushExtension
}

final class ApplePushExtension(system: ActorSystem) extends Extension with AnyRefLogSource {

  private val log = Logging(system, this)

  type Client = ApnsClient[SimpleApnsPushNotification]

  import system.dispatcher

  private lazy val db = DbExtension(system).db

  private val config = ApplePushConfig.load(
    Try(system.settings.config.getConfig("services.apple.push"))
      .getOrElse(system.settings.config.getConfig("push.apple"))
  )

  private val (clients, voipClients): (TrieMap[Int, (String, Future[Client])], TrieMap[Int, (String, Future[Client])]) = {
    val (certs, voipCerts) = config.certs.partition(!_.isVoip)
    (TrieMap(certs map createClient: _*), TrieMap(voipCerts map createClient: _*))
  }

  def clientFuture(key: Int): Option[Future[Client]] =
    clients.get(key) map {
      case (debugInfo, client) ⇒
        log.debug("Using client cert: {}", debugInfo)
        client
    }

  def voipClientFuture(key: Int): Option[Future[Client]] =
    voipClients.get(key) map {
      case (debugInfo, client) ⇒
        log.debug("Using client client: {}", debugInfo)
        client
    }

  def fetchVoipCreds(authIds: Set[Long]): Future[Seq[ApplePushCredentials]] = fetchCreds(authIds) map (_ filter (_.isVoip))

  private def fetchCreds(authIds: Set[Long]): Future[Seq[ApplePushCredentials]] = db.run(ApplePushCredentialsRepo.find(authIds))

  private def createClient(cert: ApnsCert): (Int, (String, Future[Client])) = {
    val host = cert.isSandbox match {
      case false ⇒ ApnsClient.PRODUCTION_APNS_HOST
      case true  ⇒ ApnsClient.DEVELOPMENT_APNS_HOST
    }

    val connectFuture: Future[Client] = Future {
      blocking {
        val client = new ApnsClient[SimpleApnsPushNotification](new File(cert.path), cert.password)
        client.connect(host).get(20, TimeUnit.SECONDS)
        log.debug("Established client connection for cert: {}, is voip: {}", cert.key, cert.isVoip)
        client
      }
    }

    connectFuture onFailure {
      case err ⇒
        err match {
          case e: TimeoutException ⇒
            log.warning("Timeout while waiting for client to connect: {}", e)
          case e: ExecutionException ⇒
            log.warning("Execution error on client connection: {}", e)
          case e ⇒
            log.warning("Error on client connection: {}", e)
        }
        system.scheduler.scheduleOnce(5.seconds) { recreateClient(cert) }
    }

    (cert.key, (s"key: ${cert.key}, isVoip: ${cert.isVoip}, path: ${cert.path}", connectFuture))
  }

  // recreate and try to connect client, if client connection failed
  // during previous creation
  private def recreateClient(cert: ApnsCert): Unit = {
    log.debug("Retry to create client for cert : {}, is voip", cert.key, cert.isVoip)
    val targetMap = if (cert.isVoip) voipClients else clients
    targetMap -= cert.key
    targetMap += createClient(cert)
  }

}