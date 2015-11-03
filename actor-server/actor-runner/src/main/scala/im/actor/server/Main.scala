package im.actor.server

import java.net.InetAddress

import akka.actor._
import akka.cluster.Cluster
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigException
import im.actor.config.ActorConfig
import im.actor.server.activation.gate.{ GateCodeActivation, GateConfig }
import im.actor.server.activation.internal.{ ActivationConfig, InternalCodeActivation }
import im.actor.server.api.frontend.Frontend
import im.actor.server.api.http.{ HttpApiConfig, HttpApiFrontend }
import im.actor.server.api.rpc.{ RpcApiExtension, RpcApiService }
import im.actor.server.api.rpc.service.SearchServiceImpl
import im.actor.server.api.rpc.service.auth.AuthServiceImpl
import im.actor.server.api.rpc.service.configs.ConfigsServiceImpl
import im.actor.server.api.rpc.service.contacts.ContactsServiceImpl
import im.actor.server.api.rpc.service.device.DeviceServiceImpl
import im.actor.server.api.rpc.service.files.FilesServiceImpl
import im.actor.server.api.rpc.service.groups.{ GroupInviteConfig, GroupsServiceImpl }
import im.actor.server.api.rpc.service.messaging.{ MessagingServiceImpl, ReverseHooksListener }
import im.actor.server.api.rpc.service.profile.ProfileServiceImpl
import im.actor.server.api.rpc.service.pubgroups.PubgroupsServiceImpl
import im.actor.server.api.rpc.service.push.PushServiceImpl
import im.actor.server.api.rpc.service.sequence.{ SequenceServiceConfig, SequenceServiceImpl }
import im.actor.server.api.rpc.service.users.UsersServiceImpl
import im.actor.server.api.rpc.service.weak.WeakServiceImpl
import im.actor.server.api.rpc.service.webactions.WebactionsServiceImpl
import im.actor.server.api.rpc.service.webhooks.IntegrationsServiceImpl
import im.actor.server.bot.ActorBot
import im.actor.server.cli.ActorCliService
import im.actor.server.db.DbExtension
import im.actor.server.dialog.{ DialogExtension, DialogProcessor }
import im.actor.server.email.{ EmailConfig, SmtpEmailSender }
import im.actor.server.enrich.{ RichMessageConfig, RichMessageWorker }
import im.actor.server.group._
import im.actor.server.migrations._
import im.actor.server.oauth.{ GoogleProvider, OAuth2GoogleConfig }
import im.actor.server.presences.{ GroupPresenceExtension, PresenceExtension }
import im.actor.server.sequence._
import im.actor.server.session.{ Session, SessionConfig, SessionMessage }
import im.actor.server.sms.{ TelesignCallEngine, TelesignClient, TelesignSmsEngine }
import im.actor.server.social.SocialExtension
import im.actor.server.user._

object Main extends App {
  ActorServer.start()
}

object ActorServer {
  def start(): Unit = {
    SessionMessage.register()
    CommonSerialization.register()
    UserProcessor.register()
    GroupProcessor.register()
    DialogProcessor.register()

    val serverConfig = ActorConfig.load()

    implicit val system = ActorSystem(serverConfig.getString("actor-system-name"), serverConfig)

    try {

      // FIXME: get rid of unsafe get's
      val activationConfig = ActivationConfig.load.get
      val emailConfig = EmailConfig.fromConfig(serverConfig.getConfig("services.email")).toOption.get
      val gateConfig = GateConfig.load.get
      val groupInviteConfig = GroupInviteConfig.load(serverConfig.getConfig("modules.messaging.groups.invite"))
      val webappConfig = HttpApiConfig.load(serverConfig.getConfig("webapp")).toOption.get
      val oauth2GoogleConfig = OAuth2GoogleConfig.load(serverConfig.getConfig("services.google.oauth"))
      val richMessageConfig = RichMessageConfig.load(serverConfig.getConfig("modules.enricher")).get
      val sequenceConfig = SequenceServiceConfig.load().get
      implicit val sessionConfig = SessionConfig.load(serverConfig.getConfig("session"))

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

      UserMigrator.migrate()
      GroupMigrator.migrate()
      LocalNamesMigrator.migrate()
      GroupCreatorMemberMigrator.migrate()
      HiddenGroupMigrator.migrate()
      LocalNamesFromKVMigrator.migrate()

      val weakUpdatesExt = WeakUpdatesExtension(system)
      val presenceExt = PresenceExtension(system)
      val groupPresenceExt = GroupPresenceExtension(system)
      implicit val socialManagerRegion = SocialExtension(system).region
      implicit val userProcessorRegion = UserExtension(system).processorRegion
      implicit val userViewRegion = UserExtension(system).viewRegion
      implicit val groupProcessorRegion = GroupExtension(system).processorRegion
      implicit val groupViewRegion = GroupExtension(system).viewRegion
      val groupDialogRegion = DialogExtension(system).groupRegion
      val privateDialogRegion = DialogExtension(system).privateRegion

      IntegrationTokenMigrator.migrate()

      val activationContext = serverConfig.getString("services.activation.default-service") match {
        case "internal" ⇒
          val telesignClient = new TelesignClient(serverConfig.getConfig("services.telesign"))
          InternalCodeActivation.newContext(
            activationConfig,
            new TelesignSmsEngine(telesignClient),
            new TelesignCallEngine(telesignClient),
            new SmtpEmailSender(emailConfig)
          )
        case "actor-activation" ⇒ new GateCodeActivation(gateConfig)
        case _                  ⇒ throw new Exception("""Invalid activation.default-service value provided: valid options: "internal", actor-activation""")
      }

      implicit val sessionRegion = Session.startRegion(Session.props)

      RichMessageWorker.startWorker(richMessageConfig)
      ReverseHooksListener.startSingleton()

      implicit val oauth2Service = new GoogleProvider(oauth2GoogleConfig)

      val services = Seq(
        new AuthServiceImpl(activationContext),
        new ContactsServiceImpl,
        MessagingServiceImpl(),
        new GroupsServiceImpl(groupInviteConfig),
        new PubgroupsServiceImpl,
        new SequenceServiceImpl(sequenceConfig),
        new WeakServiceImpl,
        new UsersServiceImpl,
        new FilesServiceImpl,
        new ConfigsServiceImpl,
        new PushServiceImpl,
        new ProfileServiceImpl,
        new IntegrationsServiceImpl(s"${webappConfig.scheme}://${webappConfig.host}"),
        new WebactionsServiceImpl,
        new DeviceServiceImpl,
        new SearchServiceImpl
      )

      system.log.warning("Starting ActorBot")
      ActorBot.start()

      RpcApiExtension(system).register(services)

      ActorCliService.start(system)

      Frontend.start(serverConfig)
      HttpApiFrontend.start(serverConfig)
    } catch {
      case e: ConfigException ⇒
        system.log.error(e, "Failed to load server configuration")
        throw e
      case e: Throwable ⇒
        system.log.error(e, "Server failed to start up")
        throw e
    }
  }
}