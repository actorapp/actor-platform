package im.actor.server.dialog

import im.actor.api.rpc.messaging.{ ApiTextMessage, ResponseLoadHistory }
import im.actor.api.rpc.{ Ok, AuthData, ClientData, PeersImplicits }
import im.actor.api.rpc.peers.{ ApiPeer, ApiPeerType }
import im.actor.server.acl.ACLUtils
import im.actor.server.api.rpc.service.messaging.MessagingServiceImpl
import im.actor.server.sequence.SeqStateDate
import im.actor.server.{ MessageParsing, BaseAppSuite, ImplicitAuthService, ImplicitSessionRegion }

import scala.concurrent.Future
import scala.language.postfixOps

final class DialogProcessorSpec extends BaseAppSuite
  with ImplicitAuthService
  with ImplicitSessionRegion
  with PeersImplicits
  with MessageParsing {

  behavior of "Dialog Processor"

  it should "not allow time out when there are highly frequent messages" in noTimeout()

  it should "not allow duplicated timestamp in messages" in uniqueTimestamp()

  private val dialogExt = DialogExtension(system)

  private val messService = MessagingServiceImpl()

  def noTimeout() = {
    val (alice, aliceAuthId, aliceAuthSid, _) = createUser()
    val (bob, bobAuthId, bobAuthSid, _) = createUser()

    val alicePeer = ApiPeer(ApiPeerType.Private, alice.id)
    val bobPeer = ApiPeer(ApiPeerType.Private, bob.id)

    def sendMessageToBob(text: String): Future[SeqStateDate] =
      dialogExt.sendMessage(bobPeer, alice.id, aliceAuthSid, ACLUtils.randomLong(), textMessage(text))

    def sendMessageToAlice(text: String): Future[SeqStateDate] =
      dialogExt.sendMessage(alicePeer, bob.id, bobAuthSid, ACLUtils.randomLong(), textMessage(text))

    val toAlice = for (i ← 1 to 50) yield sendMessageToAlice(s"Hello $i")
    val toBob = for (i ← 1 to 50) yield sendMessageToBob(s"Hello you back $i")

    toAlice foreach { whenReady(_)(identity) }
    toBob foreach { whenReady(_)(identity) }

    {
      implicit val clientData = ClientData(bobAuthId, 2, Some(AuthData(bob.id, bobAuthSid)))
      val aliceOutPeer = whenReady(ACLUtils.getOutPeer(alicePeer, bobAuthId))(identity)
      whenReady(messService.handleLoadHistory(aliceOutPeer, 0L, Int.MaxValue)) { resp ⇒
        inside(resp) {
          case Ok(ResponseLoadHistory(messages, _)) ⇒
            val (aliceMessages, bobsMessages) = messages map { mess ⇒
              val parsed = parseMessage(mess.message.toByteArray)
              parsed.isRight shouldEqual true
              val message = parsed.right.toOption.get
              message match {
                case ApiTextMessage(text, _, _) ⇒ text
                case _                          ⇒ fail()
              }
            } partition (mess ⇒ mess startsWith "Hello you back")
            aliceMessages should have length 50
            bobsMessages should have length 50
        }
      }
    }

  }

  def uniqueTimestamp() = {
    val (alice, aliceAuthId, aliceAuthSid, _) = createUser()
    val (bob, bobAuthId, bobAuthSid, _) = createUser()

    val alicePeer = ApiPeer(ApiPeerType.Private, alice.id)
    val bobPeer = ApiPeer(ApiPeerType.Private, bob.id)

    def sendMessageToBob(text: String): Future[SeqStateDate] =
      dialogExt.sendMessage(bobPeer, alice.id, aliceAuthSid, ACLUtils.randomLong(), textMessage(text))

    def sendMessageToAlice(text: String): Future[SeqStateDate] =
      dialogExt.sendMessage(alicePeer, bob.id, bobAuthSid, ACLUtils.randomLong(), textMessage(text))

    val toAlice = for (i ← 1 to 50) yield sendMessageToAlice(s"Hello $i")
    val toBob = for (i ← 1 to 50) yield sendMessageToBob(s"Hello you back $i")

    toAlice foreach { whenReady(_)(identity) }
    toBob foreach { whenReady(_)(identity) }

    {
      implicit val clientData = ClientData(bobAuthId, 2, Some(AuthData(bob.id, bobAuthSid)))
      val aliceOutPeer = whenReady(ACLUtils.getOutPeer(alicePeer, bobAuthId))(identity)
      whenReady(messService.handleLoadHistory(aliceOutPeer, 0L, Int.MaxValue)) { resp ⇒
        inside(resp) {
          case Ok(ResponseLoadHistory(messages, _)) ⇒
            val (aliceMessages, bobsMessages) = messages partition (m ⇒ m.senderUserId == alice.id)
            (aliceMessages map (_.date) distinct) should have length 50
            (bobsMessages map (_.date) distinct) should have length 50
          //            (messages map (_.date) distinct) should have length 100 // todo: this one is future goal. Ensure uniqueness across dialog participants
        }
      }
    }
  }

}