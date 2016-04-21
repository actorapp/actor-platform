package im.actor.server.dialog

import java.time.Instant

import akka.persistence.SnapshotMetadata
import im.actor.api.rpc.PeersImplicits
import im.actor.server.ActorSuite
import im.actor.server.cqrs.ProcessorStateProbe
import im.actor.server.model.Peer

import scala.util.Random

final class DialogProcessorStateSpec extends ActorSuite with PeersImplicits {
  it should "increase counter on NewMessage and decrease on Read" in counter

  import im.actor.server.dialog.DialogEvents._

  def counter() = {
    val userId = 1
    implicit val probe = ProcessorStateProbe(DialogState.initial(1))

    val alice = Peer.privat(2)

    val date1 = Instant.now()
    probe.commit(NewMessage(Random.nextLong(), date1, alice.id))
    probe.state.counter should be(1)

    val date2 = date1.plusMillis(1)
    probe.commit(NewMessage(Random.nextLong(), date2, alice.id))
    probe.state.counter should be(2)

    checkSnapshot(userId)

    probe.commit(MessagesRead(date1, alice.id))
    probe.state.counter should be(2)

    probe.commit(MessagesRead(date1, userId))
    probe.state.counter should be(1)

    probe.commit(NewMessage(Random.nextLong(), Instant.now(), alice.id))
    probe.commit(NewMessage(Random.nextLong(), Instant.now(), alice.id))
    probe.commit(NewMessage(Random.nextLong(), Instant.now(), alice.id))

    probe.state.counter should be(4)

    checkSnapshot(userId)
  }

  private def checkSnapshot(userId: Int)(implicit probe: ProcessorStateProbe[DialogState]) =
    DialogState.initial(userId).withSnapshot(SnapshotMetadata("", 0), probe.state.snapshot) should be(probe.state)
}