package im.actor.server

import scala.util.{ Failure, Success }

import akka.actor._
import akka.contrib.pattern.DistributedPubSubExtension
import akka.kernel.Bootable
import akka.stream.ActorMaterializer
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.transfer.TransferManager
import com.github.dwhjames.awswrap.s3.AmazonS3ScalaClient
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
import im.actor.server.oauth.{ GmailProvider, OAuth2GmailConfig }
import im.actor.server.peermanagers.{ GroupPeerManager, PrivatePeerManager }
import im.actor.server.presences.{ GroupPresenceManager, PresenceManager }
import im.actor.server.push.{ ApplePushManager, ApplePushManagerConfig, SeqUpdatesManager, WeakUpdatesManager }
import im.actor.server.session.{ Session, SessionConfig }
import im.actor.server.sms.TelesignSmsEngine
import im.actor.server.social.SocialManager
import im.actor.server.util.UploadManager
import im.actor.utils.http.DownloadManager

class Main extends Bootable with DbInit with FlywayInit {
  val config = ConfigFactory.load()
  val serverConfig = config.getConfig("actor-server")

  val activationConfig = ActivationConfig.fromConfig(serverConfig.getConfig("activation"))
  val applePushConfig = ApplePushManagerConfig.fromConfig(serverConfig.getConfig("push.apple"))
  val authConfig = AuthConfig.fromConfig(serverConfig.getConfig("auth"))
  val googlePushConfig = serverConfig.getConfig("push.google")
  val groupInviteConfig = GroupInviteConfig.fromConfig(serverConfig.getConfig("messaging.groups.invite"))
  val emailConfig = EmailConfig.fromConfig(serverConfig.getConfig("email"))
  // FIXME: get rid of Option.get
  val webappConfig = HttpApiConfig.fromConfig(serverConfig.getConfig("webapp")).toOption.get
  val ilectroInterceptionConfig = LlectroInterceptionConfig.fromConfig(serverConfig.getConfig("messaging.llectro"))
  val oauth2GmailConfig = OAuth2GmailConfig.fromConfig(serverConfig.getConfig("oauth.v2.gmail"))
  val richMessageConfig = RichMessageConfig.fromConfig(serverConfig.getConfig("enrich"))
  val s3Config = serverConfig.getConfig("files.s3")
  val sqlConfig = serverConfig.getConfig("services.postgresql")
  val smsConfig = serverConfig.getConfig("sms")
  implicit val sessionConfig = SessionConfig.fromConfig(serverConfig.getConfig("session"))

  implicit val system = ActorSystem(serverConfig.getString("actor-system-name"), serverConfig)
  implicit val executor = system.dispatcher
  implicit val materializer = ActorMaterializer()

  val ds = initDs(sqlConfig).toOption.get
  implicit val db = initDb(ds)

  def startup() = {
    val flyway = initFlyway(ds.ds)
    flyway.migrate()

    implicit val gcmSender = new Sender(googlePushConfig.getString("key"))

    implicit val apnsManager = new ApplePushManager(applePushConfig, system)

    implicit val seqUpdManagerRegion = SeqUpdatesManager.startRegion()
    implicit val weakUpdManagerRegion = WeakUpdatesManager.startRegion()
    implicit val presenceManagerRegion = PresenceManager.startRegion()
    implicit val groupPresenceManagerRegion = GroupPresenceManager.startRegion()
    implicit val socialManagerRegion = SocialManager.startRegion()
    implicit val privatePeerManagerRegion = PrivatePeerManager.startRegion()
    implicit val groupPeerManagerRegion = GroupPeerManager.startRegion()

    val s3BucketName = s3Config.getString("bucket")
    val awsKey = s3Config.getString("key")
    val awsSecret = s3Config.getString("secret")
    val awsCredentials = new BasicAWSCredentials(awsKey, awsSecret)

    implicit val client = new AmazonS3ScalaClient(awsCredentials)
    implicit val transferManager = new TransferManager(awsCredentials)

    val mediator = DistributedPubSubExtension(system).mediator

    val activationContext = Activation.newContext(activationConfig, new TelesignSmsEngine(config.getConfig("telesign")), new EmailSender(emailConfig))
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
    implicit val uploadManager = new UploadManager(s3BucketName)

    MessageInterceptor.startSingleton(ilectro, downloadManager, uploadManager, mediator, ilectroInterceptionConfig)
    RichMessageWorker.startWorker(richMessageConfig, mediator)

    implicit val oauth2Service = new GmailProvider(oauth2GmailConfig)

    val services = Seq(
      new AuthServiceImpl(activationContext, mediator, authConfig),
      new ContactsServiceImpl,
      MessagingServiceImpl(mediator),
      new GroupsServiceImpl(s3BucketName, groupInviteConfig),
      new PubgroupsServiceImpl,
      new SequenceServiceImpl,
      new WeakServiceImpl,
      new UsersServiceImpl,
      new FilesServiceImpl(s3BucketName),
      new ConfigsServiceImpl,
      new PushServiceImpl,
      new ProfileServiceImpl(s3BucketName),
      new LlectroServiceImpl(ilectro),
      new IntegrationsServiceImpl(webappConfig)
    )

    system.actorOf(RpcApiService.props(services), "rpcApiService")

    Frontend.start(serverConfig)
    HttpApiFrontend.start(serverConfig, s3BucketName)
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
