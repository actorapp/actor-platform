package im.actor.server.api.rpc.service

import im.actor.server.group.GroupOffice
import im.actor.server.user.UserOffice

import scala.concurrent.Future
import scala.util.Random

import com.amazonaws.auth.EnvironmentVariableCredentialsProvider
import com.amazonaws.services.s3.transfer.TransferManager
import org.joda.time.DateTime

import im.actor.api.rpc.Implicits._
import im.actor.api.rpc._
import im.actor.api.rpc.messaging._
import im.actor.api.rpc.misc.ResponseVoid
import im.actor.api.rpc.peers.{ GroupOutPeer, PeerType }
import im.actor.server.api.rpc.service.groups.{ GroupInviteConfig, GroupsServiceImpl }
import im.actor.server.api.rpc.service.groups.GroupsServiceImpl
import im.actor.server.oauth.{ GoogleProvider, OAuth2GoogleConfig }
import im.actor.server.{ ImplicitFileStorageAdapter, BaseAppSuite, models, persist }
import im.actor.server.presences.{ GroupPresenceManager, PresenceManager }
import im.actor.server.social.SocialManager
import im.actor.server.util.ACLUtils

class MessagingServiceHistorySpec extends BaseAppSuite with GroupsServiceHelpers with ImplicitFileStorageAdapter {
  behavior of "MessagingServiceHistoryService"

  it should "Load history (private)" in s.privat

  it should "Load dialogs" in s.dialogs // TODO: remove this test's dependency on previous example

  it should "mark messages received and send updates (private)" in s.historyPrivate.markReceived // TODO: same

  it should "mark messages read and send updates (private)" in s.historyPrivate.markRead // TODO: same

  it should "mark messages received and send updates (group)" in s.historyGroup.markReceived

  it should "mark messages read and send updates (group)" in s.historyGroup.markRead // TODO: same

  it should "Load all history in public groups" in s.public

  implicit val sessionRegion = buildSessionRegionProxy()

  implicit val seqUpdManagerRegion = buildSeqUpdManagerRegion()
  implicit val socialManagerRegion = SocialManager.startRegion()
  implicit val presenceManagerRegion = PresenceManager.startRegion()
  implicit val groupPresenceManagerRegion = GroupPresenceManager.startRegion()
  implicit val privatePeerManagerRegion = UserOffice.startRegion()
  implicit val groupPeerManagerRegion = GroupOffice.startRegion()

  val awsCredentials = new EnvironmentVariableCredentialsProvider()

  val groupInviteConfig = GroupInviteConfig("http://actor.im")

  implicit val service = messaging.MessagingServiceImpl(mediator)
  implicit val groupsService = new GroupsServiceImpl(groupInviteConfig)
  val oauthGoogleConfig = OAuth2GoogleConfig.load(system.settings.config.getConfig("services.google.oauth"))
  implicit val oauth2Service = new GoogleProvider(oauthGoogleConfig)
  implicit val authService = buildAuthService()

  object s {
    val (user1, authId1, _) = createUser()
    val sessionId1 = createSessionId()

    val (user2, authId2, _) = createUser()
    val sessionId2 = createSessionId()

    val clientData1 = ClientData(authId1, sessionId1, Some(user1.id))
    val clientData2 = ClientData(authId2, sessionId2, Some(user2.id))

    val user1Model = getUserModel(user1.id)
    val user1AccessHash = ACLUtils.userAccessHash(authId2, user1.id, user1Model.accessSalt)
    val user1Peer = peers.OutPeer(PeerType.Private, user1.id, user1AccessHash)

    val user2Model = getUserModel(user2.id)
    val user2AccessHash = ACLUtils.userAccessHash(authId1, user2.id, user2Model.accessSalt)
    val user2Peer = peers.OutPeer(PeerType.Private, user2.id, user2AccessHash)

    def privat() = {
      val step = 100L

      val (message1Date, message2Date, message3Date) = {
        implicit val clientData = clientData1

        whenReady(service.handleSendMessage(user2Peer, Random.nextLong(), TextMessage("Hi Shiva 1", Vector.empty, None)))(_ ⇒ ())

        val message1Date = System.currentTimeMillis()
        Thread.sleep(step)

        whenReady(service.handleSendMessage(user2Peer, Random.nextLong(), TextMessage("Hi Shiva 2", Vector.empty, None)))(_ ⇒ ())

        val message2Date = System.currentTimeMillis()
        Thread.sleep(step)

        whenReady(service.handleSendMessage(user2Peer, Random.nextLong(), TextMessage("Hi Shiva 3", Vector.empty, None)))(_ ⇒ ())

        val message3Date = System.currentTimeMillis()
        Thread.sleep(step)

        whenReady(service.handleSendMessage(user2Peer, Random.nextLong(), TextMessage("Hi Shiva 4", Vector.empty, None)))(_ ⇒ ())

        (message1Date, message2Date, message3Date)
      }

      {
        implicit val clientData = clientData2

        whenReady(service.handleMessageReceived(user1Peer, message2Date)) { resp ⇒
          resp should matchPattern {
            case Ok(ResponseVoid) ⇒
          }
        }

        whenReady(service.handleMessageRead(user1Peer, message1Date)) { resp ⇒
          resp should matchPattern {
            case Ok(ResponseVoid) ⇒
          }
        }
      }

      Thread.sleep(1000)

      {
        implicit val clientData = clientData1

        whenReady(service.handleLoadHistory(user2Peer, message3Date, 100)) { resp ⇒
          resp should matchPattern {
            case Ok(_) ⇒
          }
          val respBody = resp.toOption.get

          respBody.users.length should ===(0)
          respBody.history.length should ===(3)
          respBody.history.map(_.state) should ===(Seq(Some(MessageState.Sent), Some(MessageState.Received), Some(MessageState.Read)))
        }
      }
    }

    def dialogs() = {
      {
        implicit val clientData = clientData1

        whenReady(service.handleLoadDialogs(0, 100)) { resp ⇒
          resp should matchPattern {
            case Ok(_) ⇒
          }

          val respBody = resp.toOption.get

          respBody.dialogs.length should ===(1)
          val dialog = respBody.dialogs.head
          dialog.unreadCount should ===(0)
          respBody.users.length should ===(2)
        }
      }

      {
        implicit val clientData = clientData2

        whenReady(service.handleLoadDialogs(0, 100)) { resp ⇒
          resp should matchPattern {
            case Ok(_) ⇒
          }

          val respBody = resp.toOption.get

          respBody.dialogs.length should ===(1)
          val dialog = respBody.dialogs.head
          dialog.unreadCount should ===(3)
          respBody.users.length should ===(1)
        }
      }
    }

    def public() = {
      val group = models.Group(Random.nextInt, 0, Random.nextLong, "Public group", isPublic = true, new DateTime, "A public group")

      whenReady(db.run(persist.Group.create(group, Random.nextLong)))(identity)

      val groupOutPeer = GroupOutPeer(group.id, group.accessHash)

      {
        implicit val clientData = clientData1
        whenReady(groupsService.handleJoinGroupDirect(groupOutPeer))(identity)
        whenReady(service.handleSendMessage(groupOutPeer.asOutPeer, Random.nextLong(), TextMessage("First", Vector.empty, None)))(identity)
      }

      {
        implicit val clientData = clientData2
        whenReady(groupsService.handleJoinGroupDirect(groupOutPeer))(identity)
        whenReady(service.handleSendMessage(groupOutPeer.asOutPeer, Random.nextLong(), TextMessage("Second", Vector.empty, None)))(identity)

        Thread.sleep(1000)

        whenReady(service.handleLoadHistory(groupOutPeer.asOutPeer, 0, 100)) { resp ⇒
          val history = resp.toOption.get.history
          history.length should ===(4)
          history(1).message should ===(TextMessage("First", Vector.empty, None))
          history(3).message should ===(TextMessage("Second", Vector.empty, None))
        }
      }
    }

    object historyPrivate {
      val (user1, authId1, _) = createUser()
      val sessionId1 = createSessionId()

      val (user2, authId2, _) = createUser()
      val sessionId2 = createSessionId()

      val clientData1 = ClientData(authId1, sessionId1, Some(user1.id))
      val clientData2 = ClientData(authId2, sessionId2, Some(user2.id))

      val user1Model = getUserModel(user1.id)
      val user1AccessHash = ACLUtils.userAccessHash(authId2, user1.id, user1Model.accessSalt)
      val user1Peer = peers.OutPeer(PeerType.Private, user1.id, user1AccessHash)

      val user2Model = getUserModel(user2.id)
      val user2AccessHash = ACLUtils.userAccessHash(authId1, user2.id, user2Model.accessSalt)
      val user2Peer = peers.OutPeer(PeerType.Private, user2.id, user2AccessHash)

      def markReceived() = {

        val startDate = {
          implicit val clientData = clientData1

          val startDate = System.currentTimeMillis()

          val sendMessages = Future.sequence(Seq(
            service.handleSendMessage(user2Peer, Random.nextLong(), TextMessage("Hi Shiva 1", Vector.empty, None)),
            futureSleep(1500).flatMap(_ ⇒ service.handleSendMessage(user2Peer, Random.nextLong(), TextMessage("Hi Shiva 2", Vector.empty, None))),
            futureSleep(3000).flatMap(_ ⇒ service.handleSendMessage(user2Peer, Random.nextLong(), TextMessage("Hi Shiva 3", Vector.empty, None)))
          ))

          whenReady(sendMessages)(_ ⇒ ())

          startDate
        }

        {
          implicit val clientData = clientData2

          whenReady(service.handleMessageReceived(user1Peer, startDate + 2000)) { resp ⇒
            resp should matchPattern {
              case Ok(ResponseVoid) ⇒
            }
          }

          Thread.sleep(100) // Let peer managers write to db

          whenReady(db.run(persist.Dialog.find(user1.id, models.Peer.privat(user2.id)))) { dialogOpt ⇒
            dialogOpt.get.lastReceivedAt.getMillis should be < startDate + 3000
            dialogOpt.get.lastReceivedAt.getMillis should be > startDate + 1000
          }
        }

        {
          whenReady(db.run(persist.sequence.SeqUpdate.findLast(authId1))) { lastUpdate ⇒
            lastUpdate.get.header should ===(UpdateMessageReceived.header)
          }
        }
      }

      def markRead() = {
        val startDate = {
          implicit val clientData = clientData1

          val startDate = System.currentTimeMillis()

          val sendMessages = Future.sequence(Seq(
            service.handleSendMessage(user2Peer, Random.nextLong(), TextMessage("Hi Shiva 1", Vector.empty, None)),
            futureSleep(1500).flatMap(_ ⇒ service.handleSendMessage(user2Peer, Random.nextLong(), TextMessage("Hi Shiva 2", Vector.empty, None))),
            futureSleep(3000).flatMap(_ ⇒ service.handleSendMessage(user2Peer, Random.nextLong(), TextMessage("Hi Shiva 3", Vector.empty, None)))
          ))

          whenReady(sendMessages)(_ ⇒ ())

          startDate
        }

        {
          implicit val clientData = clientData2

          whenReady(service.handleMessageRead(user1Peer, startDate + 2000)) { resp ⇒
            resp should matchPattern {
              case Ok(ResponseVoid) ⇒
            }
          }

          Thread.sleep(100) // Let peer managers write to db

          whenReady(db.run(persist.Dialog.find(user1.id, models.Peer.privat(user2.id)))) { optDialog ⇒
            val dialog = optDialog.get
            dialog.lastReadAt.getMillis should be < startDate + 3000
            dialog.lastReadAt.getMillis should be > startDate + 1000
          }

          whenReady(service.handleLoadDialogs(Long.MaxValue, 100)) { resp ⇒
            val dialog = resp.toOption.get.dialogs.head

            dialog.unreadCount shouldEqual 1
          }
        }

        {
          whenReady(db.run(persist.sequence.SeqUpdate.findLast(authId1))) { lastUpdate ⇒
            lastUpdate.get.header should ===(UpdateMessageRead.header)
          }

          whenReady(db.run(persist.sequence.SeqUpdate.findLast(authId2))) { lastUpdate ⇒
            lastUpdate.get.header should ===(UpdateMessageReadByMe.header)
          }
        }
      }
    }

    object historyGroup {
      val (user1, authId1, _) = createUser()
      val sessionId1 = createSessionId()

      val (user2, authId2, _) = createUser()
      val sessionId2 = createSessionId()

      val clientData1 = ClientData(authId1, sessionId1, Some(user1.id))
      val clientData2 = ClientData(authId2, sessionId2, Some(user2.id))

      val groupOutPeer = {
        implicit val clientData = clientData1

        createGroup("Fun group", Set(user2.id)).groupPeer
      }

      def markReceived() = {
        val startDate = System.currentTimeMillis()

        {
          implicit val clientData = clientData1

          val sendMessages = Future.sequence(Seq(
            service.handleSendMessage(groupOutPeer.asOutPeer, Random.nextLong(), TextMessage("Hi Shiva 1", Vector.empty, None)),
            futureSleep(1500).flatMap(_ ⇒ service.handleSendMessage(groupOutPeer.asOutPeer, Random.nextLong(), TextMessage("Hi Shiva 2", Vector.empty, None))),
            futureSleep(3000).flatMap(_ ⇒ service.handleSendMessage(groupOutPeer.asOutPeer, Random.nextLong(), TextMessage("Hi Shiva 3", Vector.empty, None)))
          ))

          whenReady(sendMessages)(_ ⇒ ())
        }

        {
          implicit val clientData = clientData2

          whenReady(service.handleMessageReceived(groupOutPeer.asOutPeer, startDate + 2000)) { resp ⇒
            resp should matchPattern {
              case Ok(ResponseVoid) ⇒
            }
          }

          Thread.sleep(100) // Let peer managers write to db

          whenReady(db.run(persist.Dialog.find(user1.id, models.Peer.group(groupOutPeer.groupId)))) { dialogOpt ⇒
            dialogOpt.get.ownerLastReceivedAt.getMillis should be < startDate + 3000
            dialogOpt.get.ownerLastReceivedAt.getMillis should be > startDate + 1000
          }
        }

        {
          implicit val clientData = clientData1

          whenReady(db.run(persist.sequence.SeqUpdate.findLast(authId1))) { lastUpdate ⇒
            lastUpdate.get.header should ===(UpdateMessageReceived.header)
          }
        }
      }

      def markRead() = {
        val startDate = System.currentTimeMillis()

        {
          implicit val clientData = clientData1

          val sendMessages = Future.sequence(Seq(
            service.handleSendMessage(groupOutPeer.asOutPeer, Random.nextLong(), TextMessage("Hi Shiva 1", Vector.empty, None)),
            futureSleep(1500).flatMap(_ ⇒ service.handleSendMessage(groupOutPeer.asOutPeer, Random.nextLong(), TextMessage("Hi Shiva 2", Vector.empty, None))),
            futureSleep(3000).flatMap(_ ⇒ service.handleSendMessage(groupOutPeer.asOutPeer, Random.nextLong(), TextMessage("Hi Shiva 3", Vector.empty, None)))
          ))

          whenReady(sendMessages)(_ ⇒ ())
        }

        {
          implicit val clientData = clientData2

          whenReady(service.handleMessageRead(groupOutPeer.asOutPeer, startDate + 2000)) { resp ⇒
            resp should matchPattern {
              case Ok(ResponseVoid) ⇒
            }
          }

          Thread.sleep(300) // Let peer managers write to db

          whenReady(db.run(persist.Dialog.find(user1.id, models.Peer.group(groupOutPeer.groupId)))) { dialogOpt ⇒
            dialogOpt.get.lastReadAt.getMillis should be < startDate + 3000
            dialogOpt.get.lastReadAt.getMillis should be > startDate + 1000
          }

          whenReady(service.handleLoadDialogs(Long.MaxValue, 100)) { resp ⇒
            val dialog = resp.toOption.get.dialogs.head
            dialog.unreadCount shouldEqual 1
          }
        }

        {
          // Drop service message
          whenReady(db.run(persist.sequence.SeqUpdate.find(authId1) map (_.drop(1).headOption))) { updates ⇒
            val lastUpdate = updates.headOption
            lastUpdate.get.header should ===(UpdateMessageRead.header)
          }

          // Drop MessageSent for service message
          whenReady(db.run(persist.sequence.SeqUpdate.find(authId2) map (_.drop(1).headOption))) { lastUpdate ⇒
            lastUpdate.get.header should ===(UpdateMessageReadByMe.header)
          }
        }
      }
    }
  }

}
