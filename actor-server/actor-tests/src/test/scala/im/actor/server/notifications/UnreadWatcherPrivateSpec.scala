package im.actor.server.notifications

import im.actor.server.group.GroupOffice
import im.actor.server.user.UserOffice

import scala.concurrent.duration._

import im.actor.api.rpc.messaging.TextMessage
import im.actor.api.rpc.peers.PeerType
import im.actor.api.rpc.{ ClientData, peers }
import im.actor.server.BaseAppSuite
import im.actor.server.api.rpc.service.GroupsServiceHelpers
import im.actor.server.api.rpc.service.messaging.MessagingServiceImpl
import im.actor.server.oauth.{ GoogleProvider, OAuth2GoogleConfig }
import im.actor.server.social.SocialManager
import im.actor.server.util.ACLUtils

class UnreadWatcherPrivateSpec extends BaseAppSuite with GroupsServiceHelpers {

  behavior of "UnreadWatcher"

  it should "be no notifications when nothing is timed out" in tests.noNotifications()

  it should "add all subsequent messages from timed out dialog to notification" in tests.fromOneSender()

  it should "appear new message in notification after time out" in tests.fromTwoSenders()

  it should "be notifications for two users" in tests.twoNotifications()

  it should "be notifications for three users" in tests.threeNotifications()

  it should "be no notifications, when timeout setting is big" in tests.emptyWatcher()

  implicit val seqUpdManagerRegion = buildSeqUpdManagerRegion()
  implicit val socialManagerRegion = SocialManager.startRegion()
  implicit val privatePeerManagerRegion = UserOffice.startRegion()
  implicit val groupPeerManagerRegion = GroupOffice.startRegion()

  implicit val service = MessagingServiceImpl(mediator)

  implicit val sessionRegion = buildSessionRegionProxy()
  val oauthGoogleConfig = OAuth2GoogleConfig.load(system.settings.config.getConfig("services.google.oauth"))
  implicit val oauth2Service = new GoogleProvider(oauthGoogleConfig)
  implicit val authService = buildAuthService()

  implicit val notifier = new Notifier {
    def processTask(task: Notification) = ()
  }

  object tests {
    val (user1, authId1, _) = createUser()
    val sessionId1 = createSessionId()

    val (user2, authId2, _) = createUser()
    val sessionId2 = createSessionId()

    val (user3, authId3, _) = createUser()
    val sessionId3 = createSessionId()

    val clientData1 = ClientData(authId1, sessionId1, Some(user1.id))
    val clientData2 = ClientData(authId2, sessionId2, Some(user2.id))
    val clientData3 = ClientData(authId3, sessionId3, Some(user3.id))

    val user1Model = getUserModel(user1.id)
    val user2Model = getUserModel(user2.id)
    val user3Model = getUserModel(user3.id)

    //for sending messages from user2 to user1
    val user21AccessHash = ACLUtils.userAccessHash(authId2, user1.id, user1Model.accessSalt)
    val user21Peer = peers.OutPeer(PeerType.Private, user1.id, user21AccessHash)

    //for sending messages from user1 to user2
    val user12AccessHash = ACLUtils.userAccessHash(authId1, user2.id, user2Model.accessSalt)
    val user12Peer = peers.OutPeer(PeerType.Private, user2.id, user12AccessHash)

    //for sending messages from user3 to user2
    val user32AccessHash = ACLUtils.userAccessHash(authId3, user2.id, user2Model.accessSalt)
    val user32Peer = peers.OutPeer(PeerType.Private, user2.id, user32AccessHash)

    //for sending messages from user1 to user3
    val user13AccessHash = ACLUtils.userAccessHash(authId1, user3.id, user3Model.accessSalt)
    val user13Peer = peers.OutPeer(PeerType.Private, user3.id, user13AccessHash)

    implicit val clientData = clientData1

    implicit val config = UnreadWatcherConfig(4.seconds)
    val watcher = new UnreadWatcher()

    def noNotifications() = {
      whenReady(service.handleSendMessage(user12Peer, 1L, TextMessage("Hello 1", Vector.empty, None)))(_ ⇒ ())
      whenReady(watcher.getNotifications) { notifications ⇒
        notifications shouldBe empty
      }
    }

    def fromOneSender() = {
      whenReady(futureSleep(4000).flatMap(_ ⇒ watcher.getNotifications)) { notifications ⇒
        notifications should have length 1
        notifications.head shouldEqual Notification(user2.id, Map(Some(user1.name) → 1))
      }

      whenReady(service.handleSendMessage(user12Peer, 2L, TextMessage("Hello 2", Vector.empty, None)))(_ ⇒ ())
      whenReady(watcher.getNotifications) { notifications ⇒
        notifications should have length 1
        notifications.head shouldEqual Notification(user2.id, Map(Some(user1.name) → 2))
      }
    }

    def fromTwoSenders() = {
      implicit val clientData = clientData3

      //first we send message - it should not be in list - not timed out yet
      whenReady(service.handleSendMessage(user32Peer, 3L, TextMessage("Hello 3", Vector.empty, None)))(_ ⇒ ())
      whenReady(watcher.getNotifications) { notifications ⇒
        notifications should have length 1
        notifications.head shouldEqual Notification(user2.id, Map(Some(user1.name) → 2))
      }

      //then we wait - this message should appear because dialog is timed out
      whenReady(futureSleep(4000).flatMap(_ ⇒ watcher.getNotifications)) { notifications ⇒
        notifications should have length 1
        notifications.head shouldEqual Notification(user2.id, Map(Some(user1.name) → 2, Some(user3.name) → 1))
      }
    }

    def twoNotifications() = {
      implicit val clientData = clientData2

      whenReady(service.handleSendMessage(user21Peer, 4L, TextMessage("hello to you", Vector.empty, None)))(_ ⇒ ())
      //not enough time passed to be timed out
      whenReady(futureSleep(2000).flatMap(_ ⇒ watcher.getNotifications)) { notifications ⇒
        notifications should have length 1
      }

      //enough time to be timed out - one message
      whenReady(futureSleep(2000).flatMap(_ ⇒ watcher.getNotifications)) { notifications ⇒
        notifications should have length 2
        notifications should contain allOf (
          Notification(user2.id, Map(Some(user1.name) → 2, Some(user3.name) → 1)),
          Notification(user1.id, Map(Some(user2.name) → 1))
        )
      }

      //two messages from user2 to user1 should be in notifications
      whenReady(service.handleSendMessage(user21Peer, 5L, TextMessage("don't forget the milk!", Vector.empty, None)))(_ ⇒ ())
      whenReady(watcher.getNotifications) { notifications ⇒
        notifications should have length 2
        notifications should contain(Notification(user1.id, Map(Some(user2.name) → 2)))
      }
    }

    def threeNotifications() = {
      whenReady(service.handleSendMessage(user13Peer, 6L, TextMessage("Where is the money!?", Vector.empty, None)))(_ ⇒ ())
      whenReady(futureSleep(4000).flatMap(_ ⇒ watcher.getNotifications)) { notifications ⇒
        notifications should have length 3
        notifications should contain allOf (
          Notification(user2.id, Map(Some(user1.name) → 2, Some(user3.name) → 1)),
          Notification(user1.id, Map(Some(user2.name) → 2)),
          Notification(user3.id, Map(Some(user1.name) → 1))
        )
      }
    }

    def emptyWatcher() = {
      val bigTimeoutWatcher = {
        implicit val config = UnreadWatcherConfig(10.minutes)
        new UnreadWatcher()
      }

      whenReady(bigTimeoutWatcher.getNotifications) { notifications ⇒
        notifications shouldBe empty
      }
    }
  }

}
