package im.actor.server.dialog

import java.time.Instant

import akka.persistence.SnapshotMetadata
import im.actor.api.rpc.PeersImplicits
import im.actor.server.ActorSuite
import im.actor.server.cqrs.ProcessorStateProbe
import im.actor.server.model.Peer

final class DialogRootStateSpec extends ActorSuite with PeersImplicits {
  it should "sort dialogs by appearing" in show
  it should "remove Favourites on Unfavourite" in favouriteUnfavourite
  it should "remove from Archived on Favourite or new message" in removeFromArchived
  it should "order archived by date desc" in archivedOrder

  import DialogRootEvents._

  def show() = {
    implicit val probe = ProcessorStateProbe(DialogRootState.initial)

    val alice = Peer.privat(1)
    val bob = Peer.privat(2)

    probe.commit(Created(Instant.now(), Some(alice)))
    probe.commit(Created(Instant.now().plusMillis(1), Some(bob)))

    getActivePeers should be(Seq(alice, bob))

    getGroupPeers(DialogGroupType.DirectMessages) should be(Seq(alice, bob))
    checkSnapshot
  }

  def favouriteUnfavourite() = {
    implicit val probe = ProcessorStateProbe(DialogRootState.initial)

    val alice = Peer.privat(1)

    probe.commit(Favourited(Instant.now, Some(alice)))
    getGroupPeers(DialogGroupType.DirectMessages) shouldNot contain(alice)

    probe.commit(Unfavourited(Instant.now, Some(alice)))

    getGroupPeers(DialogGroupType.DirectMessages) should contain(alice)
    checkSnapshot
  }

  def removeFromArchived() = {
    implicit val probe = ProcessorStateProbe(DialogRootState.initial)

    val alice = Peer.privat(1)

    probe.commit(Archived(Instant.now, Some(alice)))
    getArchivedPeers should be(Seq(alice))
    checkSnapshot

    probe.commit(Unarchived(Instant.now, Some(alice)))
    getArchivedPeers should be(Seq.empty)
    getGroupPeers(DialogGroupType.DirectMessages) should be(Seq(alice))
    checkSnapshot

    probe.commit(Archived(Instant.now, Some(alice)))
    getGroupPeers(DialogGroupType.DirectMessages) should be(Seq.empty)
    checkSnapshot

    probe.commit(Favourited(Instant.now, Some(alice)))
    getArchivedPeers should be(Seq.empty)
    getGroupPeers(DialogGroupType.Favourites) should be(Seq(alice))
    checkSnapshot
  }

  def archivedOrder() = {
    implicit val probe = ProcessorStateProbe(DialogRootState.initial)

    val alice = Peer.privat(1)
    val bob = Peer.privat(2)

    probe.commit(Archived(Instant.now, Some(alice)))
    probe.commit(Archived(Instant.now, Some(bob)))

    getArchivedPeers should be(Seq(bob, alice))
    checkSnapshot
  }

  private def getGroupPeers(typ: DialogGroupType)(implicit probe: ProcessorStateProbe[DialogRootState]) =
    typ match {
      case DialogGroupType.Favourites     ⇒ probe.state.active.favourites.toSeq.map(_.peer)
      case DialogGroupType.Groups         ⇒ probe.state.active.groups.toSeq.map(_.peer)
      case DialogGroupType.DirectMessages ⇒ probe.state.active.dms.toSeq.map(_.peer)
      case unknown                        ⇒ throw DialogErrors.UnknownDialogGroupType(unknown)
    }

  private def getActivePeers(implicit probe: ProcessorStateProbe[DialogRootState]) =
    probe.state.activePeers.toSeq.map(_.peer)

  private def getArchivedPeers(implicit probe: ProcessorStateProbe[DialogRootState]) =
    probe.state.archived.toSeq.map(_.peer)

  private def checkSnapshot(implicit probe: ProcessorStateProbe[DialogRootState]) =
    DialogRootState.initial.withSnapshot(SnapshotMetadata("", 0), probe.state.snapshot) should be(probe.state)
}