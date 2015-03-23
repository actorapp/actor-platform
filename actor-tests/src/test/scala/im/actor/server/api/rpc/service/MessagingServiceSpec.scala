package im.actor.server.api.rpc.service

import scala.concurrent._, duration._

import slick.dbio.DBIO

import im.actor.api.{ rpc => api }, api.Implicits._
import im.actor.server.api.util
import im.actor.server.persist
import im.actor.server.push.SeqUpdatesManager

class MessagingServiceSpec extends BaseServiceSpec {
  def is = sequential ^ s2"""
  SendMessage handler should
    send messages ${s.privat.sendMessage}
  """

  object s {
    val seqUpdManagerRegion = SeqUpdatesManager.startRegion()

    implicit val service = new messaging.MessagingServiceImpl(seqUpdManagerRegion)
    implicit val authService = buildAuthService()
    implicit val ec = system.dispatcher

    object privat {
      val (user, authId, _) = createUser()
      implicit val clientData = api.ClientData(authId, Some(user.id))

      val (user2, _, _) = createUser()
      val user2Model = getUserModel(user2.id)
      val user2AccessHash = util.ACL.userAccessHash(authId, user2.id, user2Model.accessSalt)
      val user2Peer = api.peers.OutPeer(api.peers.PeerType.Private, user2.id, user2AccessHash)

      def sendMessage = {
        service.handleSendMessage(user2Peer, 1L, api.messaging.TextMessage("Hi Shiva", 0, None).toMessageContent) must beOkLike {
          case api.misc.ResponseSeqDate(1002, _, _) => ok
        }.await
      }
    }
  }
}
