package im.actor.server

import java.net.InetAddress

import akka.actor._
import akka.cluster.Cluster
import akka.contrib.pattern.DistributedPubSubExtension
import akka.stream.ActorMaterializer
import im.actor.config.ActorConfig
import im.actor.server.activation.gate.{ GateCodeActivation, GateConfig }
import im.actor.server.activation.internal.{ ActivationConfig, InternalCodeActivation }
import im.actor.server.api.frontend.Frontend
import im.actor.server.api.http.{ HttpApiConfig, HttpApiFrontend }
import im.actor.server.api.rpc.RpcApiService
import im.actor.server.api.rpc.service.auth.AuthServiceImpl
import im.actor.server.api.rpc.service.configs.ConfigsServiceImpl
import im.actor.server.api.rpc.service.contacts.ContactsServiceImpl
import im.actor.server.api.rpc.service.files.FilesServiceImpl
import im.actor.server.api.rpc.service.groups.{ GroupInviteConfig, GroupsServiceImpl }
import im.actor.server.api.rpc.service.messaging.{ ReverseHooksListener, MessagingServiceImpl }
import im.actor.server.api.rpc.service.profile.ProfileServiceImpl
import im.actor.server.api.rpc.service.pubgroups.PubgroupsServiceImpl
import im.actor.server.api.rpc.service.push.PushServiceImpl
import im.actor.server.api.rpc.service.sequence.{ SequenceServiceConfig, SequenceServiceImpl }
import im.actor.server.api.rpc.service.users.UsersServiceImpl
import im.actor.server.api.rpc.service.weak.WeakServiceImpl
import im.actor.server.api.rpc.service.webhooks.IntegrationsServiceImpl
import im.actor.server.db.DbExtension
import im.actor.server.dialog.privat.{ PrivateDialog, PrivateDialogExtension }
import im.actor.server.email.{ EmailConfig, EmailSender }
import im.actor.server.enrich.{ RichMessageConfig, RichMessageWorker }
import im.actor.server.group._
import im.actor.server.migrations.{ GroupCreatorMemberMigrator, IntegrationTokenMigrator, LocalNamesMigrator }
import im.actor.server.oauth.{ GoogleProvider, OAuth2GoogleConfig }
import im.actor.server.dialog.group.{ GroupDialog, GroupDialogExtension }
import im.actor.server.presences.{ GroupPresenceManager, PresenceManager }
import im.actor.server.sequence._
import im.actor.server.session.{ Session, SessionConfig, SessionMessage }
import im.actor.server.sms.TelesignSmsEngine
import im.actor.server.social.SocialExtension
import im.actor.server.user._

object Main extends App {
  SessionMessage.register()
  CommonSerialization.register()
  UserProcessor.register()
  GroupProcessor.register()
  GroupDialog.register()
  PrivateDialog.register()

  val serverConfig = ActorConfig.load()

  // FIXME: get rid of unsafe get's
  val activationConfig = ActivationConfig.load.get
  val emailConfig = EmailConfig.fromConfig(serverConfig.getConfig("services.email")).toOption.get
  val gateConfig = GateConfig.load.get
  val groupInviteConfig = GroupInviteConfig.load(serverConfig.getConfig("enabled-modules.messaging.groups.invite"))
  val webappConfig = HttpApiConfig.load(serverConfig.getConfig("webapp")).toOption.get
  val oauth2GoogleConfig = OAuth2GoogleConfig.load(serverConfig.getConfig("services.google.oauth"))
  val richMessageConfig = RichMessageConfig.load(serverConfig.getConfig("enabled-modules.enricher")).get
  val smsConfig = serverConfig.getConfig("sms")
  val sequenceConfig = SequenceServiceConfig.load().get
  implicit val sessionConfig = SessionConfig.load(serverConfig.getConfig("session"))

  implicit val system = ActorSystem(serverConfig.getString("actor-system-name"), serverConfig)

  if (system.settings.config.getList("akka.cluster.seed-nodes").isEmpty) {
    system.log.info("Going to a single-node cluster mode")
    val self = Address(
      "akka.tcp",
      system.name,
      InetAddress.getLocalHost.getHostAddress,
      2552
    )

    Cluster(system).joinSeedNodes(List(self))
  }

  implicit val executor = system.dispatcher
  implicit val materializer = ActorMaterializer()

  implicit val db = DbExtension(system).db

  DbExtension(system).migrate()

  UserMigrator.migrate()
  GroupMigrator.migrate()
  LocalNamesMigrator.migrate()
  GroupCreatorMemberMigrator.migrate()

  implicit val weakUpdManagerRegion = WeakUpdatesManager.startRegion()
  implicit val presenceManagerRegion = PresenceManager.startRegion()
  implicit val groupPresenceManagerRegion = GroupPresenceManager.startRegion()
  implicit val socialManagerRegion = SocialExtension(system).region
  implicit val userProcessorRegion = UserExtension(system).processorRegion
  implicit val userViewRegion = UserExtension(system).viewRegion
  implicit val groupProcessorRegion = GroupExtension(system).processorRegion
  implicit val groupViewRegion = GroupExtension(system).viewRegion
  implicit val groupDialogRegion = GroupDialogExtension(system).region //no need to be implicit
  implicit val privateDialogRegion = PrivateDialogExtension(system).region

  IntegrationTokenMigrator.migrate()

  val mediator = DistributedPubSubExtension(system).mediator

  val activationContext = serverConfig.getString("services.activation.default-service") match {
    case "internal" ⇒ InternalCodeActivation.newContext(
      activationConfig,
      new TelesignSmsEngine(serverConfig.getConfig("services.telesign")),
      new EmailSender(emailConfig)
    )
    case "actor-activation" ⇒ new GateCodeActivation(gateConfig)
    case _                  ⇒ throw new Exception("""Invalid activation.default-service value provided: valid options: "internal", actor-activation""")
  }

  implicit val sessionRegion = Session.startRegion(
    Some(Session.props(mediator))
  )

  RichMessageWorker.startWorker(richMessageConfig, mediator)
  ReverseHooksListener.startSingleton(mediator)

  implicit val oauth2Service = new GoogleProvider(oauth2GoogleConfig)

  val services = Seq(
    new AuthServiceImpl(activationContext, mediator),
    new ContactsServiceImpl,
    MessagingServiceImpl(mediator),
    new GroupsServiceImpl(groupInviteConfig),
    new PubgroupsServiceImpl,
    new SequenceServiceImpl(sequenceConfig),
    new WeakServiceImpl,
    new UsersServiceImpl,
    new FilesServiceImpl,
    new ConfigsServiceImpl,
    new PushServiceImpl,
    new ProfileServiceImpl,
    new IntegrationsServiceImpl(webappConfig)
  )

  system.actorOf(RpcApiService.props(services), "rpcApiService")

  Frontend.start(serverConfig)
  HttpApiFrontend.start(serverConfig)
}