package im.actor.server.dialog

import java.time.Instant

import im.actor.api.rpc.PeersImplicits
import im.actor.server.ActorSuite
import im.actor.server.cqrs.ProcessorStateProbe
import im.actor.server.model.Peer

final class DialogRootStateSpec extends ActorSuite with PeersImplicits {
  it should "have DMs and Groups by default" in default
  it should "sort dialogs by appearing" in show
  it should "remove Favourites on Unfavourite" in favouriteUnfavourite
  it should "remove from Archived on Favourite or new message" in removeFromArchived

  import DialogRootEvents._

  def default() = {
    val probe = ProcessorStateProbe(DialogRootState.initial)
    probe.state.active.keys should be(Set(DialogGroupType.Groups, DialogGroupType.DirectMessages))
  }

  def show() = {
    implicit val probe = ProcessorStateProbe(DialogRootState.initial)

    val alice = Peer.privat(1)
    val bob = Peer.privat(2)

    probe.commit(Created(Instant.now(), Some(alice)))
    probe.commit(Created(Instant.now().plusMillis(1), Some(bob)))

    getActivePeers should be(Seq(alice, bob))

    getGroupPeers(DialogGroupType.DirectMessages) should be(Seq(alice, bob))
  }

  def favouriteUnfavourite() = {
    implicit val probe = ProcessorStateProbe(DialogRootState.initial)

    val alice = Peer.privat(1)

    probe.commit(Favourited(Instant.now, Some(alice)))
    getGroupPeers(DialogGroupType.DirectMessages) shouldNot contain(alice)

    probe.commit(Unfavourited(Instant.now, Some(alice)))

    probe.state.active.keys shouldNot contain(DialogGroupType.Favourites)
    getGroupPeers(DialogGroupType.DirectMessages) should contain(alice)
  }

  def removeFromArchived() = {
    implicit val probe = ProcessorStateProbe(DialogRootState.initial)

    val alice = Peer.privat(1)

    probe.commit(Archived(Instant.now, Some(alice)))
    getArchivedPeers should be(Seq(alice))

    probe.commit(Unarchived(Instant.now, Some(alice)))
    getArchivedPeers should be(Seq.empty)
    getGroupPeers(DialogGroupType.DirectMessages) should be(Seq(alice))

    probe.commit(Archived(Instant.now, Some(alice)))
    getGroupPeers(DialogGroupType.DirectMessages) should be(Seq.empty)

    probe.commit(Favourited(Instant.now, Some(alice)))
    getArchivedPeers should be(Seq.empty)
    getGroupPeers(DialogGroupType.Favourites) should be(Seq(alice))
  }

  private def getGroupPeers(typ: DialogGroupType)(implicit probe: ProcessorStateProbe[DialogRootState]) =
    probe.state.active.get(typ).get.toSeq.map(_.peer)

  private def getActivePeers(implicit probe: ProcessorStateProbe[DialogRootState]) =
    probe.state.activePeers.toSeq.map(_.peer)

  private def getArchivedPeers(implicit probe: ProcessorStateProbe[DialogRootState]) =
    probe.state.archived.toSeq.map(_.peer)
}