package im.actor.server.api.rpc.service

import im.actor.api.rpc.Implicits._
import im.actor.api.rpc._
import im.actor.api.rpc.messaging.{ TextMessage, UpdateMessage }
import im.actor.api.rpc.misc.ResponseSeqDate
import im.actor.api.rpc.peers.PeerType
import im.actor.server.api.rpc.service.groups.GroupsServiceImpl
import im.actor.server.api.util
import im.actor.server.persist
import im.actor.server.push.{ WeakUpdatesManager, SeqUpdatesManager }

class MessagingServiceSpec extends BaseServiceSuite with GroupsServiceHelpers {
  behavior of "MessagingService"

  it should "send messages" in s.privat.sendMessage

  it should "send group messages" in s.group.sendMessage

  object s {
    val seqUpdManagerRegion = SeqUpdatesManager.startRegion()
    val weakUpdManagerRegion = WeakUpdatesManager.startRegion()
    val rpcApiService = buildRpcApiService()
    val sessionRegion = buildSessionRegion(rpcApiService, seqUpdManagerRegion, weakUpdManagerRegion)

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

  }

}
