package im.actor.server.api.rpc.service

import scala.concurrent.{ ExecutionContext, Future }

import im.actor.api.rpc.Implicits._
import im.actor.api.rpc._
import im.actor.api.rpc.messaging._
import im.actor.api.rpc.misc.{ ResponseVoid, ResponseSeqDate }
import im.actor.api.rpc.peers.PeerType
import im.actor.server.api.rpc.service.groups.GroupsServiceImpl
import im.actor.server.api.util
import im.actor.server.models
import im.actor.server.persist
import im.actor.server.presences.PresenceManager
import im.actor.server.push.{ WeakUpdatesManager, SeqUpdatesManager }

class MessagingServiceSpec extends BaseServiceSuite with GroupsServiceHelpers {
  behavior of "MessagingService"

  "Messaging" should "send messages" in s.privat.sendMessage

  it should "send group messages" in s.group.sendMessage

  "History" should "mark messages received and send updates (private)" in s.historyPrivate.markReceived

  it should "mark messages read and send updates (private)" in s.historyPrivate.markRead

  it should "mark messages received and send updates (group)" in s.historyGroup.markReceived

  it should "mark messages read and send updates (group)" in s.historyGroup.markRead

  object s {
    val seqUpdManagerRegion = SeqUpdatesManager.startRegion()
    val weakUpdManagerRegion = WeakUpdatesManager.startRegion()
    val presenceManagerRegion = PresenceManager.startRegion()
    val rpcApiService = buildRpcApiService()
    val sessionRegion = buildSessionRegion(rpcApiService, seqUpdManagerRegion, weakUpdManagerRegion, presenceManagerRegion)

    implicit val service = new messaging.MessagingServiceImpl(seqUpdManagerRegion)
    implicit val groupsService = new GroupsServiceImpl(seqUpdManagerRegion)
    implicit val authService = buildAuthService(sessionRegion)
    implicit val ec = system.dispatcher

    object privat {
      val (user, authId, _) = createUser()
      val sessionId = createSessionId()
      implicit val clientData = ClientData(authId, sessionId, Some(user.id))

      val (user2, _, _) = createUser()
      val user2Model = getUserModel(user2.id)
      val user2AccessHash = util.ACL.userAccessHash(authId, user2.id, user2Model.accessSalt)
      val user2Peer = peers.OutPeer(PeerType.Private, user2.id, user2AccessHash)

      def sendMessage() = {
        whenReady(service.handleSendMessage(user2Peer, 1L, TextMessage("Hi Shiva", 0, None).toMessageContent)) { resp =>
          resp should matchPattern {
            case Ok(ResponseSeqDate(1000, _, _)) =>
          }
        }
      }
    }

    object group {
      val (user1, authId1, _) = createUser()
      val (user2, authId2, _) = createUser()
      val sessionId = createSessionId()
      implicit val clientData = ClientData(authId1, sessionId, Some(user1.id))

      val groupOutPeer = createGroup("Fun group", Set(user2.id)).groupPeer

      def sendMessage() = {
        whenReady(service.handleSendMessage(groupOutPeer.asOutPeer, 2L, TextMessage("Hi again", 0, None).toMessageContent)) { resp =>
          resp should matchPattern {
            case Ok(ResponseSeqDate(1001, _, _)) =>
          }
        }


        whenReady(db.run(persist.sequence.SeqUpdate.find(authId2).head)) { u =>
          u.header should ===(UpdateMessage.header)
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
      val user1AccessHash = util.ACL.userAccessHash(authId2, user1.id, user1Model.accessSalt)
      val user1Peer = peers.OutPeer(PeerType.Private, user1.id, user1AccessHash)

      val user2Model = getUserModel(user2.id)
      val user2AccessHash = util.ACL.userAccessHash(authId1, user2.id, user2Model.accessSalt)
      val user2Peer = peers.OutPeer(PeerType.Private, user2.id, user2AccessHash)

      def markReceived() = {


        val startDate = {
          implicit val clientData = clientData1

          val startDate = System.currentTimeMillis()

          val sendMessages = Future.sequence(Seq(
            service.handleSendMessage(user2Peer, 1L, TextMessage("Hi Shiva 1", 0, None).toMessageContent),
            futureSleep(1500).flatMap(_ => service.handleSendMessage(user2Peer, 2L, TextMessage("Hi Shiva 2", 0, None).toMessageContent)),
            futureSleep(3000).flatMap(_ => service.handleSendMessage(user2Peer, 3L, TextMessage("Hi Shiva 3", 0, None).toMessageContent))
          ))

          whenReady(sendMessages)(_ => ())

          startDate
        }

        {
          implicit val clientData = clientData2

          whenReady(service.handleMessageReceived(user1Peer, startDate + 2000)) { resp =>
            resp should matchPattern {
              case Ok(ResponseVoid) =>
            }
          }

          whenReady(db.run(persist.Dialog.find(user1.id, models.Peer.privat(user2.id)).head)) { dialog =>
            dialog.lastReceivedAt.getMillis should be < startDate + 3000
            dialog.lastReceivedAt.getMillis should be > startDate + 1000
          }
        }

        {
          whenReady(db.run(persist.sequence.SeqUpdate.find(authId1).head)) { lastUpdate =>
            lastUpdate.header should ===(UpdateMessageReceived.header)
          }
        }
      }

      def markRead() = {
        val startDate = {
          implicit val clientData = clientData1

          val startDate = System.currentTimeMillis()

          val sendMessages = Future.sequence(Seq(
            service.handleSendMessage(user2Peer, 1L, TextMessage("Hi Shiva 1", 0, None).toMessageContent),
            futureSleep(1500).flatMap(_ => service.handleSendMessage(user2Peer, 2L, TextMessage("Hi Shiva 2", 0, None).toMessageContent)),
            futureSleep(3000).flatMap(_ => service.handleSendMessage(user2Peer, 3L, TextMessage("Hi Shiva 3", 0, None).toMessageContent))
          ))

          whenReady(sendMessages)(_ => ())

          startDate
        }

        {
          implicit val clientData = clientData2

          whenReady(service.handleMessageRead(user1Peer, startDate + 2000)) { resp =>
            resp should matchPattern {
              case Ok(ResponseVoid) =>
            }
          }

          whenReady(db.run(persist.Dialog.find(user1.id, models.Peer.privat(user2.id)).head)) { dialog =>
            dialog.lastReadAt.getMillis should be < startDate + 3000
            dialog.lastReadAt.getMillis should be > startDate + 1000
          }
        }

        {
          whenReady(db.run(persist.sequence.SeqUpdate.find(authId1).head)) { lastUpdate =>
            lastUpdate.header should ===(UpdateMessageRead.header)
          }

          whenReady(db.run(persist.sequence.SeqUpdate.find(authId2).head)) { lastUpdate =>
            lastUpdate.header should ===(UpdateMessageReadByMe.header)
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
            service.handleSendMessage(groupOutPeer.asOutPeer, 1L, TextMessage("Hi Shiva 1", 0, None).toMessageContent),
            futureSleep(1500).flatMap(_ => service.handleSendMessage(groupOutPeer.asOutPeer, 2L, TextMessage("Hi Shiva 2", 0, None).toMessageContent)),
            futureSleep(3000).flatMap(_ => service.handleSendMessage(groupOutPeer.asOutPeer, 3L, TextMessage("Hi Shiva 3", 0, None).toMessageContent))
          ))

          whenReady(sendMessages)(_ => ())
        }

        {
          implicit val clientData = clientData2

          whenReady(service.handleMessageReceived(groupOutPeer.asOutPeer, startDate + 2000)) { resp =>
            resp should matchPattern {
              case Ok(ResponseVoid) =>
            }
          }

          whenReady(db.run(persist.Dialog.find(user1.id, models.Peer.group(groupOutPeer.groupId)).head)) { dialog =>
            dialog.lastReceivedAt.getMillis should be < startDate + 3000
            dialog.lastReceivedAt.getMillis should be > startDate + 1000
          }
        }

        {
          implicit val clientData = clientData1

          whenReady(db.run(persist.sequence.SeqUpdate.find(authId1).head)) { lastUpdate =>
            lastUpdate.header should ===(UpdateMessageReceived.header)
          }
        }
      }

      def markRead() = {
        val startDate = System.currentTimeMillis()

        {
          implicit val clientData = clientData1

          val sendMessages = Future.sequence(Seq(
            service.handleSendMessage(groupOutPeer.asOutPeer, 1L, TextMessage("Hi Shiva 1", 0, None).toMessageContent),
            futureSleep(1500).flatMap(_ => service.handleSendMessage(groupOutPeer.asOutPeer, 2L, TextMessage("Hi Shiva 2", 0, None).toMessageContent)),
            futureSleep(3000).flatMap(_ => service.handleSendMessage(groupOutPeer.asOutPeer, 3L, TextMessage("Hi Shiva 3", 0, None).toMessageContent))
          ))

          whenReady(sendMessages)(_ => ())
        }

        {
          implicit val clientData = clientData2

          whenReady(service.handleMessageRead(groupOutPeer.asOutPeer, startDate + 2000)) { resp =>
            resp should matchPattern {
              case Ok(ResponseVoid) =>
            }
          }

          whenReady(db.run(persist.Dialog.find(user1.id, models.Peer.group(groupOutPeer.groupId)).head)) { dialog =>
            dialog.lastReadAt.getMillis should be < startDate + 3000
            dialog.lastReadAt.getMillis should be > startDate + 1000
          }
        }

        {
          whenReady(db.run(persist.sequence.SeqUpdate.find(authId1).head)) { lastUpdate =>
            lastUpdate.header should ===(UpdateMessageRead.header)
          }

          whenReady(db.run(persist.sequence.SeqUpdate.find(authId2).head)) { lastUpdate =>
            lastUpdate.header should ===(UpdateMessageReadByMe.header)
          }
        }
      }
    }
  }
}
