package im.actor.server

import scala.util.{ Failure, Success }

import akka.actor._
import akka.contrib.pattern.DistributedPubSubExtension
import akka.kernel.Bootable
import akka.stream.ActorMaterializer
import com.google.android.gcm.server.Sender
import com.typesafe.config.ConfigFactory

import im.actor.server.activation.{ Activation, ActivationConfig }
import im.actor.server.api.frontend.Frontend
import im.actor.server.api.http.{ HttpApiConfig, HttpApiFrontend }
import im.actor.server.api.rpc.RpcApiService
import im.actor.server.api.rpc.service.auth.{ AuthConfig, AuthServiceImpl }
import im.actor.server.api.rpc.service.configs.ConfigsServiceImpl
import im.actor.server.api.rpc.service.contacts.ContactsServiceImpl
import im.actor.server.api.rpc.service.files.FilesServiceImpl
import im.actor.server.api.rpc.service.groups.{ GroupInviteConfig, GroupsServiceImpl }
import im.actor.server.api.rpc.service.llectro.interceptors.MessageInterceptor
import im.actor.server.api.rpc.service.llectro.{ LlectroInterceptionConfig, LlectroServiceImpl }
import im.actor.server.api.rpc.service.messaging.MessagingServiceImpl
import im.actor.server.api.rpc.service.profile.ProfileServiceImpl
import im.actor.server.api.rpc.service.pubgroups.PubgroupsServiceImpl
import im.actor.server.api.rpc.service.push.PushServiceImpl
import im.actor.server.api.rpc.service.sequence.SequenceServiceImpl
import im.actor.server.api.rpc.service.users.UsersServiceImpl
import im.actor.server.api.rpc.service.weak.WeakServiceImpl
import im.actor.server.api.rpc.service.webhooks.IntegrationsServiceImpl
import im.actor.server.db.{ DbInit, FlywayInit }
import im.actor.server.llectro.Llectro
import im.actor.server.email.{ EmailConfig, EmailSender }
import im.actor.server.enrich.{ RichMessageConfig, RichMessageWorker }
import im.actor.server.oauth.{ GoogleProvider, OAuth2GoogleConfig }
import im.actor.server.peermanagers.{ GroupPeerManager, PrivatePeerManager }
import im.actor.server.presences.{ GroupPresenceManager, PresenceManager }
import im.actor.server.push._
import im.actor.server.session.{ Session, SessionConfig }
import im.actor.server.sms.TelesignSmsEngine
import im.actor.server.social.SocialManager
import im.actor.server.util.{ S3StorageAdapter, S3StorageAdapterConfig }
import im.actor.utils.http.DownloadManager

class Main extends Bootable with DbInit with FlywayInit {
  val serverConfig = ConfigFactory.load()

  // FIXME: get rid of unsafe get's
  val activationConfig = ActivationConfig.fromConfig(serverConfig.getConfig("services.activation")).toOption.get
  val applePushConfig = ApplePushManagerConfig.load(serverConfig.getConfig("push.apple"))
  val authConfig = AuthConfig.load.get
  val emailConfig = EmailConfig.fromConfig(serverConfig.getConfig("services.email")).toOption.get
  val googlePushConfig = GooglePushManagerConfig.load(serverConfig.getConfig("services.google.push")).get
  val groupInviteConfig = GroupInviteConfig.load(serverConfig.getConfig("enabled-modules.messaging.groups.invite"))
  val webappConfig = HttpApiConfig.load(serverConfig.getConfig("webapp")).toOption.get
  val ilectroInterceptionConfig = LlectroInterceptionConfig.load(serverConfig.getConfig("messaging.llectro"))
  val oauth2GoogleConfig = OAuth2GoogleConfig.load(serverConfig.getConfig("services.google.oauth"))
  val richMessageConfig = RichMessageConfig.load(serverConfig.getConfig("enabled-modules.enricher")).get
  val s3StorageAdapterConfig = S3StorageAdapterConfig.load(serverConfig.getConfig("services.aws.s3")).get
  val sqlConfig = serverConfig.getConfig("services.postgresql")
  val smsConfig = serverConfig.getConfig("sms")
  implicit val sessionConfig = SessionConfig.load(serverConfig.getConfig("session"))

  implicit val system = ActorSystem(serverConfig.getString("actor-system-name"), serverConfig)
  implicit val executor = system.dispatcher
  implicit val materializer = ActorMaterializer()

  val ds = initDs(sqlConfig).toOption.get
  implicit val db = initDb(ds)

  def startup() = {
    val flyway = initFlyway(ds.ds)
    flyway.migrate()

    implicit val googlePushManager = new GooglePushManager(googlePushConfig)

    implicit val apnsManager = new ApplePushManager(applePushConfig, system)

    implicit val seqUpdManagerRegion = SeqUpdatesManager.startRegion()
    implicit val weakUpdManagerRegion = WeakUpdatesManager.startRegion()
    implicit val presenceManagerRegion = PresenceManager.startRegion()
    implicit val groupPresenceManagerRegion = GroupPresenceManager.startRegion()
    implicit val socialManagerRegion = SocialManager.startRegion()
    implicit val privatePeerManagerRegion = PrivatePeerManager.startRegion()
    implicit val groupPeerManagerRegion = GroupPeerManager.startRegion()

    implicit val fsAdapter = new S3StorageAdapter(s3StorageAdapterConfig)

    val mediator = DistributedPubSubExtension(system).mediator

    val activationContext = Activation.newContext(activationConfig, new TelesignSmsEngine(serverConfig.getConfig("services.telesign")), new EmailSender(emailConfig))

    Session.startRegion(
      Some(Session.props(mediator))
    )

    implicit val sessionRegion = Session.startRegionProxy()

    val ilectro = new Llectro
    ilectro.getAndPersistInterests() onComplete {
      case Success(i) ⇒ system.log.debug("Loaded {} interests", i)
      case Failure(e) ⇒ system.log.error(e, "Failed to load interests")
    }

    val downloadManager = new DownloadManager

    MessageInterceptor.startSingleton(ilectro, downloadManager, mediator, ilectroInterceptionConfig)
    RichMessageWorker.startWorker(richMessageConfig, mediator)

    implicit val oauth2Service = new GoogleProvider(oauth2GoogleConfig)

    val services = Seq(
      new AuthServiceImpl(activationContext, mediator, authConfig),
      new ContactsServiceImpl,
      MessagingServiceImpl(mediator),
      new GroupsServiceImpl(groupInviteConfig),
      new PubgroupsServiceImpl,
      new SequenceServiceImpl,
      new WeakServiceImpl,
      new UsersServiceImpl,
      new FilesServiceImpl,
      new ConfigsServiceImpl,
      new PushServiceImpl,
      new ProfileServiceImpl,
      new LlectroServiceImpl(ilectro),
      new IntegrationsServiceImpl(webappConfig)
    )

    system.actorOf(RpcApiService.props(services), "rpcApiService")

    Frontend.start(serverConfig)
    HttpApiFrontend.start(serverConfig)
  }

  def shutdown() = {
    system.shutdown()
    ds.close()
  }
}

object Main {
  def main(args: Array[String]): Unit = {
    new Main()
      .startup()
  }
}
