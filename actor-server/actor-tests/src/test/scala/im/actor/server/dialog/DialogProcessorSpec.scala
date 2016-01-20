package im.actor.server.dialog

import akka.testkit.TestProbe
import im.actor.api.rpc.PeersImplicits
import im.actor.server.acl.ACLUtils
import im.actor.server.dialog.DialogCommands.{ SendMessageAck, SendMessage }
import im.actor.server.model.Peer
import im.actor.server.{ BaseAppSuite, ImplicitAuthService, ImplicitSessionRegion }

import scala.concurrent.duration._

final class DialogProcessorSpec extends BaseAppSuite with ImplicitAuthService with ImplicitSessionRegion with PeersImplicits {

  behavior of "Dialog Processor"

  it should "not allow deadlocks with high frequent messages" in e1()

  def e1() = {
    val (alice, aliceAuthId, aliceAuthSid, _) = createUser()
    val (bob, bobAuthId, bobAuthSid, _) = createUser()

    val alicePeer = Peer.privat(alice.id)
    val bobPeer = Peer.privat(bob.id)

    val aliceDialog = system.actorOf(DialogProcessor.props(alice.id, bobPeer, Seq.empty), s"Private_dialog_with_bob")
    val bobDialog = system.actorOf(DialogProcessor.props(bob.id, alicePeer, Seq.empty), s"Private_dialog_with_alice")

    val probe = TestProbe()

    def sendMessageToAlice(text: String) =
      aliceDialog.tell(SendMessage(bobPeer, alicePeer, bobAuthSid, System.currentTimeMillis, ACLUtils.randomLong(), textMessage(text)), probe.ref)

    def sendMessageToBob(text: String) =
      bobDialog.tell(SendMessage(alicePeer, bobPeer, aliceAuthSid, System.currentTimeMillis, ACLUtils.randomLong(), textMessage(text)), probe.ref)

    // 3 messages to alice
    for (i ← 1 to 3) { sendMessageToAlice(s"Hello $i") }
    // 1 message to bob
    sendMessageToBob("Hello you back")

    // 4 messages to alice
    for (i ← 1 to 4) { sendMessageToAlice(s"How are you $i") }
    // 2 messages to bob
    sendMessageToBob("Well, I am fine 1")
    sendMessageToBob("Well, I am fine 2")

    // 4 messages to alice
    for (i ← 1 to 4) { sendMessageToAlice(s"Mee too $i") }

    // 1 message to bob
    sendMessageToBob("Cool")

    // 2 messages to alice
    for (i ← 1 to 2) { sendMessageToAlice(s"Well bye $i") }

    // 3 + 1 + 4 + 2 + 4 + 1 + 2 = 17 messages
    val expectThese = Array.fill(17)(classOf[SendMessageAck])

    probe.expectMsgAllConformingOf(20.seconds, expectThese: _*)
  }

}
