package im.actor.server.notifications

import scala.concurrent.duration._

import com.amazonaws.auth.EnvironmentVariableCredentialsProvider
import com.amazonaws.services.s3.transfer.TransferManager

import im.actor.api.rpc.ClientData
import im.actor.api.rpc.Implicits._
import im.actor.api.rpc.messaging.TextMessage
import im.actor.server.BaseAppSuite
import im.actor.server.api.rpc.service.GroupsServiceHelpers
import im.actor.server.api.rpc.service.auth.AuthSmsConfig
import im.actor.server.api.rpc.service.groups.{ GroupInviteConfig, GroupsServiceImpl }
import im.actor.server.api.rpc.service.messaging.MessagingServiceImpl
import im.actor.server.oauth.{ GmailProvider, OAuth2GmailConfig }
import im.actor.server.peermanagers.{ GroupPeerManager, PrivatePeerManager }
import im.actor.server.presences.{ GroupPresenceManager, PresenceManager }
import im.actor.server.social.SocialManager

class UnreadWatcherGroupSpec extends BaseAppSuite with GroupsServiceHelpers {

  behavior of "UnreadWatcher"

  it should "be no service messages in group" in tests.e1()

  implicit val seqUpdManagerRegion = buildSeqUpdManagerRegion()
  implicit val socialManagerRegion = SocialManager.startRegion()
  implicit val privatePeerManagerRegion = PrivatePeerManager.startRegion()
  implicit val groupPeerManagerRegion = GroupPeerManager.startRegion()
  implicit val presenceManagerRegion = PresenceManager.startRegion()
  implicit val groupPresenceManagerRegion = GroupPresenceManager.startRegion()

  val bucketName = "actor-uploads-test"
  val awsCredentials = new EnvironmentVariableCredentialsProvider()
  implicit val transferManager = new TransferManager(awsCredentials)
  val groupInviteConfig = GroupInviteConfig("http://actor.im")

  implicit val service = MessagingServiceImpl(mediator)
  implicit val groupService = new GroupsServiceImpl(bucketName, groupInviteConfig)

  implicit val sessionRegion = buildSessionRegionProxy()
  val oauth2GmailConfig = OAuth2GmailConfig.fromConfig(system.settings.config.getConfig("oauth.v2.gmail"))
  implicit val oauth2Service = new GmailProvider(oauth2GmailConfig)
  implicit val authSmsConfig = AuthSmsConfig.fromConfig(system.settings.config.getConfig("auth"))
  implicit val authService = buildAuthService()

  implicit val notifier = new Notifier {
    def processTask(task: Notification) = ()
  }

  object tests {
    val (user1, authId1, _) = createUser()
    val (user2, authId2, _) = createUser()
    val (user3, authId3, _) = createUser()

    val sessionId1 = createSessionId()

    implicit val clientData1 = ClientData(authId1, sessionId1, Some(user1.id))

    implicit val config = UnreadWatcherConfig(4.seconds)
    val watcher = new UnreadWatcher()

    val groupName = "Fun group"
    val groupOutPeer = createGroup(groupName, Set(user2.id, user3.id)).groupPeer

    def e1() = {
      whenReady(service.handleSendMessage(groupOutPeer.asOutPeer, 1L, TextMessage("Hello 1", Vector.empty, None)))(_ ⇒ ())
      whenReady(futureSleep(4000).flatMap(_ ⇒ watcher.getNotifications)) { notifications ⇒
        notifications should have length 2
        notifications should contain allOf (
          Notification(user2.id, Map(Some(groupName) → 1)),
          Notification(user3.id, Map(Some(groupName) → 1))
        )
      }
    }
  }

}
