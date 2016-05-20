package im.actor.server

import java.nio.file.Files

import akka.actor._
import akka.cluster.Cluster
import akka.stream.ActorMaterializer
import com.typesafe.config.{ Config, ConfigException, ConfigFactory }
import im.actor.config.ActorConfig
import im.actor.env.ActorEnv
import im.actor.server.api.http.{ HttpApi, HttpApiConfig }
import im.actor.server.api.rpc.RpcApiExtension
import im.actor.server.api.rpc.service.auth.AuthServiceImpl
import im.actor.server.api.rpc.service.configs.ConfigsServiceImpl
import im.actor.server.api.rpc.service.contacts.ContactsServiceImpl
import im.actor.server.api.rpc.service.device.DeviceServiceImpl
import im.actor.server.api.rpc.service.encryption.EncryptionServiceImpl
import im.actor.server.api.rpc.service.eventbus.EventbusServiceImpl
import im.actor.server.api.rpc.service.features.FeaturesServiceImpl
import im.actor.server.api.rpc.service.files.FilesServiceImpl
import im.actor.server.api.rpc.service.groups.{ GroupInviteConfig, GroupsServiceImpl }
import im.actor.server.api.rpc.service.messaging.MessagingServiceImpl
import im.actor.server.api.rpc.service.privacy.PrivacyServiceImpl
import im.actor.server.api.rpc.service.profile.ProfileServiceImpl
import im.actor.server.api.rpc.service.pubgroups.PubgroupsServiceImpl
import im.actor.server.api.rpc.service.push.PushServiceImpl
import im.actor.server.api.rpc.service.sequence.{ SequenceServiceConfig, SequenceServiceImpl }
import im.actor.server.api.rpc.service.stickers.StickersServiceImpl
import im.actor.server.api.rpc.service.users.UsersServiceImpl
import im.actor.server.api.rpc.service.weak.WeakServiceImpl
import im.actor.server.api.rpc.service.webactions.WebactionsServiceImpl
import im.actor.server.api.rpc.service.webhooks.IntegrationsServiceImpl
import im.actor.server.api.rpc.service.webrtc.WebrtcServiceImpl
import im.actor.server.bot.{ ActorBot, BotExtension }
import im.actor.server.cli.ActorCliService
import im.actor.server.db.DbExtension
import im.actor.server.dialog.{ DialogExtension, DialogProcessor }
import im.actor.server.enrich.{ RichMessageConfig, RichMessageWorker }
import im.actor.server.frontend.Frontend
import im.actor.server.group._
import im.actor.server.migrations._
import im.actor.server.oauth.{ GoogleProvider, OAuth2GoogleConfig }
import im.actor.server.presences.{ GroupPresenceExtension, PresenceExtension }
import im.actor.server.sequence._
import im.actor.server.session.{ Session, SessionConfig, SessionMessage }
import im.actor.server.social.SocialExtension
import im.actor.server.stickers.StickerMessages
import im.actor.server.user._
import im.actor.server.webhooks.WebhooksExtension
import kamon.Kamon

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.language.existentials

final case class ActorServer(system: ActorSystem)

object ActorServer {
  /**
   * Creates a new Actor Server builder
   *
   * @return
   */
  def newBuilder: ActorServerBuilder = ActorServerBuilder()
}

final case class ActorServerBuilder(defaultConfig: Config = ConfigFactory.empty()) {
  /**
   *
   * @param config
   * @return a builder with provided default config
   */
  def withDefaultConfig(config: Config) = this.copy(defaultConfig = config)

  /**
   * Starts a server
   *
   * @return
   */
  def start(): ActorServer = {
    val configPath = ActorEnv.getAbsolutePath("conf/server.conf")
    if (Files.exists(configPath)) {
      System.setProperty("config.file", configPath.toString)
    }

    System.setProperty("sun.jnu.encoding", "UTF-8")
    System.setProperty("file.encoding", "UTF-8")

    Kamon.start()
    SessionMessage.register()
    CommonSerialization.register()
    UserProcessor.register()
    GroupProcessor.register()
    StickerMessages.register()

    val serverConfig = ActorConfig.load(defaultConfig)

    implicit val system = ActorSystem(serverConfig.getString("actor-system-name"), serverConfig)

    try {

      // FIXME: get rid of unsafe get's
      val groupInviteConfig = GroupInviteConfig.load(serverConfig.getConfig("modules.messaging.groups.invite"))
      val httpConfig = HttpApiConfig.load(serverConfig.getConfig("http")).toOption.get
      val oauth2GoogleConfig = OAuth2GoogleConfig.load(serverConfig.getConfig("services.google.oauth"))
      val richMessageConfig = RichMessageConfig.load(serverConfig.getConfig("modules.enricher")).get
      val sequenceConfig = SequenceServiceConfig.load().get
      implicit val sessionConfig = SessionConfig.load(serverConfig.getConfig("session"))

      if (system.settings.config.getList("akka.cluster.seed-nodes").isEmpty) {
        system.log.info("Going to a single-node cluster mode")

        Cluster(system).join(Cluster(system).selfAddress)
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
      FillUserSequenceMigrator.migrate()
      FixUserSequenceMigrator.migrate()

      system.log.debug("Starting SeqUpdatesExtension")
      val seqUpdatesExt = SeqUpdatesExtension(system)

      system.log.debug("Starting WeakUpdatesExtension")
      val weakUpdatesExt = WeakUpdatesExtension(system)

      system.log.debug("Starting PresenceExtension")
      val presenceExt = PresenceExtension(system)

      system.log.debug("Starting GroupPresenceExtension")
      val groupPresenceExt = GroupPresenceExtension(system)

      system.log.debug("Starting DialogExtension")
      val dialogExt = DialogExtension(system)

      system.log.debug("Starting SocialExtension")
      implicit val socialManagerRegion = SocialExtension(system).region

      system.log.debug("Starting UserExtension")
      implicit val userProcessorRegion = UserExtension(system).processorRegion
      implicit val userViewRegion = UserExtension(system).viewRegion

      system.log.debug("Starting GroupExtension")
      implicit val groupProcessorRegion = GroupExtension(system).processorRegion
      implicit val groupViewRegion = GroupExtension(system).viewRegion

      system.log.debug("Starting IntegrationTokenMigrator")
      IntegrationTokenMigrator.migrate()

      system.log.warning("Starting session region")
      implicit val sessionRegion = Session.startRegion(Session.props)

      system.log.debug("Starting RichMessageWorker")
      RichMessageWorker.startWorker(richMessageConfig)
      // system.log.debug("Starting ReverseHooksListener")
      // ReverseHooksListener.startSingleton()

      system.log.debug("Starting WebhooksExtension")
      val webhooksExt = WebhooksExtension(system)

      implicit val oauth2Service = new GoogleProvider(oauth2GoogleConfig)

      system.log.debug("Starting Services")

      system.log.debug("Starting AuthService")
      val authService = new AuthServiceImpl

      system.log.debug("Staring ContactsService")
      val contactsService = new ContactsServiceImpl

      system.log.debug("Starting MessagingService")
      val messagingService = MessagingServiceImpl()

      system.log.debug("Starting GroupsService")
      val groupsService = new GroupsServiceImpl(groupInviteConfig)

      system.log.debug("Starting PubgroupsService")
      val pubgroupsService = new PubgroupsServiceImpl

      system.log.debug("Starting SequenceService")
      val sequenceService = new SequenceServiceImpl(sequenceConfig)

      system.log.debug("Starting WeakService")
      val weakService = new WeakServiceImpl

      system.log.debug("Starting UsersService")
      val usersService = new UsersServiceImpl

      system.log.debug("Starting FilesService")
      val filesService = new FilesServiceImpl

      system.log.debug("Starting ConfigsService")
      val configsService = new ConfigsServiceImpl

      system.log.debug("Starting PushService")
      val pushService = new PushServiceImpl

      system.log.debug("Starting ProfileService")
      val profileService = new ProfileServiceImpl

      system.log.debug("Starting IntegrationsService")
      val integrationsService = new IntegrationsServiceImpl(httpConfig.baseUri)

      system.log.debug("Starting WebactionsService")
      val webactionsService = new WebactionsServiceImpl

      system.log.debug("Starting DeviceService")
      val deviceService = new DeviceServiceImpl

      system.log.debug("Starting StickersServiceImpl")
      val stickerService = new StickersServiceImpl

      system.log.debug("Starting FeaturesServiceImpl")
      val featuresService = new FeaturesServiceImpl

      system.log.debug("Starting WebrtcServiceImpl")
      val webrtcService = new WebrtcServiceImpl()

      system.log.debug("Starting EncryptionServiceImpl")
      val encryptionService = new EncryptionServiceImpl

      system.log.debug("Starting EventbusServiceImpl")
      val eventbusService = new EventbusServiceImpl(system)

      system.log.debug("Starting PrivacyServiceImpl")
      val privacyService = new PrivacyServiceImpl

      val services = Seq(
        authService,
        contactsService,
        messagingService,
        groupsService,
        pubgroupsService,
        sequenceService,
        weakService,
        usersService,
        filesService,
        configsService,
        pushService,
        profileService,
        integrationsService,
        webactionsService,
        deviceService,
        stickerService,
        featuresService,
        webrtcService,
        encryptionService,
        eventbusService,
        privacyService
      )

      system.log.warning("Starting BotExtension")
      BotExtension(system)

      system.log.warning("Starting ActorBot")
      ActorBot.start()

      system.log.debug("Registering services")
      RpcApiExtension(system).register(services)

      system.log.debug("Starting Actor CLI")
      ActorCliService.start(system)

      system.log.debug("Starting Frontend")
      Frontend.start(serverConfig)

      system.log.debug("Starting Http Api")
      HttpApi(system).start()

      ActorServerModules(system).startModules()

      ActorServer(system)
    } catch {
      case e: ConfigException ⇒
        system.log.error(e, "Failed to load server configuration")
        system.terminate()
        Await.result(system.whenTerminated, Duration.Inf)
        System.exit(1)
        throw e
      case e: Throwable ⇒
        system.log.error(e, "Server failed to start up")
        system.terminate()
        Await.result(system.whenTerminated, Duration.Inf)
        System.exit(1)
        throw e
    }
  }
}