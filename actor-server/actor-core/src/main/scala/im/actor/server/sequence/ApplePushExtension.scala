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

  // there are some apple push keys, that require topic(bundleId)
  // to be included in apple push notification. We provide apns key -> bundle id
  // mapping for them.
  val apnsBundleId: Map[Int, String] = (config.certs collect {
    case ApnsCert(Some(key), Some(bundleId), _, _, _, _) ⇒ key → bundleId
  }).toMap

  private val (clients, voipClients): (TrieMap[String, Future[Client]], TrieMap[String, Future[Client]]) = {
    val (certs, voipCerts) = config.certs.partition(!_.isVoip)
    (createClients(certs), createClients(voipCerts))
  }

  def client(id: String): Option[Future[Client]] = clients.get(id)

  def voipClient(id: String): Option[Future[Client]] = voipClients.get(id)

  def fetchVoipCreds(authIds: Set[Long]): Future[Seq[ApplePushCredentials]] = fetchCreds(authIds) map (_ filter (_.isVoip))

  private def fetchCreds(authIds: Set[Long]): Future[Seq[ApplePushCredentials]] = db.run(ApplePushCredentialsRepo.find(authIds))

  private def createClient(cert: ApnsCert): Future[Client] = {
    val host = cert.isSandbox match {
      case false ⇒ ApnsClient.PRODUCTION_APNS_HOST
      case true  ⇒ ApnsClient.DEVELOPMENT_APNS_HOST
    }

    val connectFuture: Future[Client] = Future {
      blocking {
        val client = new ApnsClient[SimpleApnsPushNotification](new File(cert.path), cert.password)
        client.connect(host).get(20, TimeUnit.SECONDS)
        log.debug("Established client connection for cert: {}, is voip: {}", extractCertKey(cert), cert.isVoip)
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

    connectFuture
  }

  // recreate and try to connect client, if client connection failed
  // during previous creation
  private def recreateClient(cert: ApnsCert): Unit = {
    val certKey = extractCertKey(cert)
    log.debug("Retry to create client for cert : {}, is voip: {}", certKey, cert.isVoip)
    val targetMap = if (cert.isVoip) voipClients else clients
    targetMap -= certKey
    targetMap += certKey → createClient(cert)
  }

  private def createClients(certs: List[ApnsCert]): TrieMap[String, Future[Client]] =
    TrieMap(certs map (c ⇒ extractCertKey(c) → createClient(c)): _*)

  private def extractCertKey(cert: ApnsCert): String = (cert.key, cert.bundleId) match {
    case (Some(key), _)      ⇒ key.toString
    case (_, Some(bundleId)) ⇒ bundleId
    case _                   ⇒ throw new RuntimeException("Wrong cert format, no apns key, no bundle id")
  }

}