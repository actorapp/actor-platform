package im.actor.server.dialog

import im.actor.api.rpc.messaging._
import im.actor.api.rpc.peers.{ ApiPeer, ApiPeerType }
import im.actor.api.rpc.{ AuthData, ClientData, Ok, PeersImplicits }
import im.actor.server._
import im.actor.server.acl.ACLUtils
import im.actor.server.api.rpc.service.messaging.MessagingServiceImpl
import im.actor.server.sequence.SeqStateDate

import scala.concurrent.Future
import scala.language.postfixOps

final class DialogProcessorSpec extends BaseAppSuite
  with ImplicitAuthService
  with ImplicitSessionRegion
  with PeersImplicits
  with MessageParsing {

  behavior of "Dialog Processor"

  it should "pass reads after read with later date came from another user" in passReads()

  it should "not allow time out when there are highly frequent messages" in noTimeout()

  it should "not allow duplicated timestamp in messages" in uniqueTimestamp()

  private val messService = MessagingServiceImpl()

  def passReads() = {
    val (alice, aliceAuthId, aliceAuthSid, _) = createUser()
    val (bob, bobAuthId, bobAuthSid, _) = createUser()

    val alicePeer = ApiPeer(ApiPeerType.Private, alice.id)
    val bobPeer = ApiPeer(ApiPeerType.Private, bob.id)

    def sendMessageToBob(text: String): Future[SeqStateDate] =
      dialogExt.sendMessage(bobPeer, alice.id, aliceAuthSid, Some(aliceAuthId), ACLUtils.randomLong(), textMessage(text))

    def sendMessageToAlice(text: String): Future[SeqStateDate] =
      dialogExt.sendMessage(alicePeer, bob.id, bobAuthSid, Some(bobAuthId), ACLUtils.randomLong(), textMessage(text))

    val dateToAlice = whenReady(sendMessageToAlice("Hi"))(_.date)
    val dateToBob = whenReady(sendMessageToBob("Hi"))(_.date)

    whenReady(dialogExt.messageRead(alicePeer, bob.id, 0, dateToBob))(identity)
    whenReady(dialogExt.getDialogInfo(alice.id, bobPeer.asModel)) { info ⇒
      info.counter should be(1)
    }

    Thread.sleep(1)

    whenReady(dialogExt.messageRead(bobPeer, alice.id, 0, dateToAlice))(identity)
    whenReady(dialogExt.getDialogInfo(alice.id, bobPeer.asModel)) { info ⇒
      info.counter should be(0)
    }
  }

  def noTimeout() = {
    val (alice, aliceAuthId, aliceAuthSid, _) = createUser()
    val (bob, bobAuthId, bobAuthSid, _) = createUser()

    val alicePeer = ApiPeer(ApiPeerType.Private, alice.id)
    val bobPeer = ApiPeer(ApiPeerType.Private, bob.id)

    def sendMessageToBob(text: String): Future[SeqStateDate] =
      dialogExt.sendMessage(bobPeer, alice.id, aliceAuthSid, Some(aliceAuthId), ACLUtils.randomLong(), textMessage(text))

    def sendMessageToAlice(text: String): Future[SeqStateDate] =
      dialogExt.sendMessage(alicePeer, bob.id, bobAuthSid, Some(bobAuthId), ACLUtils.randomLong(), textMessage(text))

    val toAlice = for (i ← 1 to 50) yield sendMessageToAlice(s"Hello $i")
    val toBob = for (i ← 1 to 50) yield sendMessageToBob(s"Hello you back $i")

    toAlice foreach {
      whenReady(_)(identity)
    }
    toBob foreach {
      whenReady(_)(identity)
    }

    {
      implicit val clientData = ClientData(bobAuthId, 2, Some(AuthData(bob.id, bobAuthSid, 42)))
      val aliceOutPeer = whenReady(ACLUtils.getOutPeer(alicePeer, bobAuthId))(identity)
      whenReady(messService.handleLoadHistory(aliceOutPeer, 0L, None, Int.MaxValue, Vector.empty)) { resp ⇒
        inside(resp) {
          case Ok(ResponseLoadHistory(messages, _, _, _, _)) ⇒
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
      dialogExt.sendMessage(bobPeer, alice.id, aliceAuthSid, Some(aliceAuthId), ACLUtils.randomLong(), textMessage(text))

    def sendMessageToAlice(text: String): Future[SeqStateDate] =
      dialogExt.sendMessage(alicePeer, bob.id, bobAuthSid, Some(bobAuthId), ACLUtils.randomLong(), textMessage(text))

    val toAlice = for (i ← 1 to 50) yield sendMessageToAlice(s"Hello $i")
    val toBob = for (i ← 1 to 50) yield sendMessageToBob(s"Hello you back $i")

    toAlice foreach {
      whenReady(_)(identity)
    }
    toBob foreach {
      whenReady(_)(identity)
    }

    {
      implicit val clientData = ClientData(bobAuthId, 2, Some(AuthData(bob.id, bobAuthSid, 42)))
      val aliceOutPeer = whenReady(ACLUtils.getOutPeer(alicePeer, bobAuthId))(identity)
      whenReady(messService.handleLoadHistory(aliceOutPeer, 0L, None, Int.MaxValue, Vector.empty)) { resp ⇒
        inside(resp) {
          case Ok(ResponseLoadHistory(messages, _, _, _, _)) ⇒
            val (aliceMessages, bobsMessages) = messages partition (m ⇒ m.senderUserId == alice.id)
            (aliceMessages map (_.date) distinct) should have length 50
            (bobsMessages map (_.date) distinct) should have length 50
          //            (messages map (_.date) distinct) should have length 100 // todo: this one is future goal. Ensure uniqueness across dialog participants
        }
      }
    }
  }

}