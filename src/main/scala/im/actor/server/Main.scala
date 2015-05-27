package im.actor.server

import scala.util.{ Success, Failure }

import akka.actor._
import akka.contrib.pattern.DistributedPubSubExtension
import akka.kernel.Bootable
import akka.stream.ActorFlowMaterializer
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.transfer.TransferManager
import com.github.dwhjames.awswrap.s3.AmazonS3ScalaClient
import com.google.android.gcm.server.Sender
import com.typesafe.config.ConfigFactory

import im.actor.server.api.frontend.{ TcpFrontend, WsFrontend }
import im.actor.server.api.rpc.RpcApiService
import im.actor.server.api.rpc.service.auth.AuthServiceImpl
import im.actor.server.api.rpc.service.configs.ConfigsServiceImpl
import im.actor.server.api.rpc.service.contacts.ContactsServiceImpl
import im.actor.server.api.rpc.service.files.FilesServiceImpl
import im.actor.server.api.rpc.service.groups.{ GroupInviteConfig, GroupsServiceImpl }
import im.actor.server.api.rpc.service.messaging.{ GroupPeerManager, PrivatePeerManager, MessagingServiceImpl }
import im.actor.server.api.rpc.service.groups.GroupsServiceImpl
import im.actor.server.api.rpc.service.llectro.{ MessageInterceptor, IlectroServiceImpl }
import im.actor.server.api.rpc.service.messaging.MessagingServiceImpl
import im.actor.server.api.rpc.service.profile.ProfileServiceImpl
import im.actor.server.api.rpc.service.push.PushServiceImpl
import im.actor.server.api.rpc.service.sequence.SequenceServiceImpl
import im.actor.server.api.rpc.service.users.UsersServiceImpl
import im.actor.server.api.rpc.service.weak.WeakServiceImpl
import im.actor.server.db.{ DbInit, FlywayInit }
import im.actor.server.ilectro.ILectro
import im.actor.server.presences.{ GroupPresenceManager, PresenceManager }
import im.actor.server.push.{ ApplePushManager, ApplePushManagerConfig, SeqUpdatesManager, WeakUpdatesManager }
import im.actor.server.enrich.{ RichMessageConfig, RichMessageWorker }
import im.actor.server.session.{ Session, SessionConfig }
import im.actor.server.sms.SmsActivation
import im.actor.server.social.SocialManager
import im.actor.server.util.UploadManager
import im.actor.server.webhooks.{ WebhooksConfig, WebhooksFrontend }
import im.actor.utils.http.DownloadManager

class Main extends Bootable with DbInit with FlywayInit {
  val config = ConfigFactory.load()
  val serverConfig = config.getConfig("actor-server")

  val applePushConfig = ApplePushManagerConfig.fromConfig(serverConfig.getConfig("push.apple"))
  val googlePushConfig = serverConfig.getConfig("push.google")
  val groupInviteConfig = GroupInviteConfig.fromConfig(serverConfig.getConfig("messaging.groups.invite"))
  val richMessageConfig = RichMessageConfig.fromConfig(serverConfig.getConfig("enrich"))
  val s3Config = serverConfig.getConfig("files.s3")
  val sqlConfig = serverConfig.getConfig("persist.sql")
  val smsConfig = serverConfig.getConfig("sms")
  val webhooksConfig = serverConfig.getConfig("webhooks")
  implicit val sessionConfig = SessionConfig.fromConfig(serverConfig.getConfig("session"))

  implicit val system = ActorSystem(serverConfig.getString("actor-system-name"), serverConfig)
  implicit val executor = system.dispatcher
  implicit val materializer = ActorFlowMaterializer()

  val ds = initDs(sqlConfig)
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

    val activationContext = SmsActivation.newContext(smsConfig)

    Session.startRegion(
      Some(Session.props)
    )

    implicit val sessionRegion = Session.startRegionProxy()

    val ilectro = new ILectro
    ilectro.getAndPersistInterests() onComplete {
      case Success(i) ⇒ system.log.debug("Loaded {} interests", i)
      case Failure(e) ⇒ system.log.error(e, "Failed to load interests")
    }

    val downloadManager = new DownloadManager
    implicit val uploadManager = new UploadManager(s3BucketName)
    MessageInterceptor.startSingleton(ilectro, downloadManager, uploadManager)

    val mediator = DistributedPubSubExtension(system).mediator

    val messagingService = MessagingServiceImpl(mediator)

    RichMessageWorker.startWorker(richMessageConfig, mediator)

    val services = Seq(
      new AuthServiceImpl(activationContext),
      new ContactsServiceImpl,
      messagingService,
      new GroupsServiceImpl(s3BucketName, groupInviteConfig),
      new SequenceServiceImpl,
      new WeakServiceImpl,
      new UsersServiceImpl,
      new FilesServiceImpl(s3BucketName),
      new ConfigsServiceImpl,
      new PushServiceImpl,
      new ProfileServiceImpl(s3BucketName),
      new IlectroServiceImpl(ilectro)
    )

    system.actorOf(RpcApiService.props(services), "rpcApiService")

    WebhooksFrontend.start(WebhooksConfig.fromConfig(webhooksConfig), messagingService)
    TcpFrontend.start(serverConfig, sessionRegion)
    WsFrontend.start(serverConfig, sessionRegion)
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
