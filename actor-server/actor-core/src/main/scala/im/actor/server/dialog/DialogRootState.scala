package im.actor.server.dialog

import java.time.Instant

import akka.persistence.SnapshotMetadata
import im.actor.server.cqrs.{ Event, ProcessorState, TaggedEvent }
import im.actor.server.model.{ Peer, PeerErrors, PeerType }

import scala.collection.SortedSet

trait DialogRootEvent extends TaggedEvent {
  val ts: Instant

  override def tags: Set[String] = Set("dialogRoot")
}

trait DialogRootCommand

trait DialogRootQuery

private object SortableDialog {
  val OrderingAsc = new Ordering[SortableDialog] {
    override def compare(x: SortableDialog, y: SortableDialog): Int = {
      if (x.peer == y.peer) 0
      else if (x.ts.isBefore(y.ts)) -1
      else if (x.ts.isAfter(y.ts)) 1
      else 0
    }
  }

  val OrderingDesc = new Ordering[SortableDialog] {
    override def compare(x: SortableDialog, y: SortableDialog): Int =
      if (x.peer == y.peer) 0
      else if (x.ts.isBefore(y.ts)) 1
      else if (x.ts.isAfter(y.ts)) -1
      else 0
  }
}

private case class SortableDialog(ts: Instant, peer: Peer) {
  override def hashCode = peer.hashCode()

  override def equals(obj: Any) =
    obj match {
      case sd: SortableDialog ⇒ sd.peer == peer
      case _                  ⇒ false
    }
}

private object ActiveDialogs {
  val empty = ActiveDialogs(
    Set.empty,
    Set.empty,
    Set.empty
  )
}

private[dialog] case class ActiveDialogs(
  favourites: Set[Peer],
  groups:     Set[Peer],
  dms:        Set[Peer]
) {
  def withPeer(peer: Peer) = {
    if (favourites.contains(peer)) this
    else
      peer.typ match {
        case PeerType.Private ⇒ copy(dms = dms + peer)
        case PeerType.Group   ⇒ copy(groups = groups + peer)
        case unknown          ⇒ throw new PeerErrors.UnknownPeerType(unknown)
      }
  }

  def withoutPeer(peer: Peer) = {
    peer.typ match {
      case PeerType.Private ⇒ copy(dms = dms - peer, favourites = favourites - peer)
      case PeerType.Group   ⇒ copy(groups = groups - peer, favourites = favourites - peer)
      case unknown          ⇒ throw PeerErrors.UnknownPeerType(unknown)
    }
  }

  def withFavouritedPeer(peer: Peer) = {
    peer.typ match {
      case PeerType.Private ⇒ copy(dms = dms - peer, favourites = favourites + peer)
      case PeerType.Group   ⇒ copy(groups = groups - peer, favourites = favourites + peer)
      case unknown          ⇒ throw PeerErrors.UnknownPeerType(unknown)
    }
  }

  def withUnfavouritedPeer(peer: Peer) = {
    peer.typ match {
      case PeerType.Private ⇒ copy(dms = dms + peer, favourites = favourites - peer)
      case PeerType.Group   ⇒ copy(groups = groups + peer, favourites = favourites - peer)
      case unknown          ⇒ throw PeerErrors.UnknownPeerType(unknown)
    }
  }

  def withDeletedPeer(peer: Peer) = {
    peer.typ match {
      case PeerType.Private ⇒ copy(dms = dms - peer, favourites = favourites - peer)
      case PeerType.Group   ⇒ copy(groups = groups - peer, favourites = favourites - peer)
      case unknown          ⇒ throw PeerErrors.UnknownPeerType(unknown)
    }
  }

  def exists(f: Peer ⇒ Boolean) = favourites.exists(f) || groups.exists(f) || dms.exists(f)

  def map[A](f: Peer ⇒ A) = favourites.map(f) ++ groups.map(f) ++ dms.map(f)

  def find(f: Peer ⇒ Boolean) = favourites.find(f).getOrElse(groups.find(f).getOrElse(dms.find(f)))

  def contains(peer: Peer) = favourites.contains(peer) || groups.contains(peer) || dms.contains(peer)
}

private object DialogRootState {
  def initial(userId: Int) = DialogRootState(
    userId = userId,
    active = ActiveDialogs.empty,
    mobile = SortedSet.empty(SortableDialog.OrderingDesc),
    mobilePeers = Set.empty[Peer],
    archived = SortedSet.empty(SortableDialog.OrderingDesc)
  )
}

private[dialog] final case class DialogRootState(
  userId:      Int,
  active:      ActiveDialogs,
  mobile:      SortedSet[SortableDialog],
  mobilePeers: Set[Peer],
  archived:    SortedSet[SortableDialog]
) extends ProcessorState[DialogRootState] {
  import DialogRootEvents._

  override def updated(e: Event): DialogRootState = e match {
    case Created(ts, Some(peer))      ⇒ withNewPeer(ts, peer)
    case Archived(ts, Some(peer))     ⇒ withArchivedPeer(ts, peer)
    case Unarchived(ts, Some(peer))   ⇒ withUnarchivedPeer(ts, peer)
    case Favourited(ts, Some(peer))   ⇒ withFavouritedPeer(ts, peer)
    case Unfavourited(ts, Some(peer)) ⇒ withUnfavouritedPeer(ts, peer)
    case Deleted(ts, Some(peer))      ⇒ withDeletedPeer(ts, peer)
    case Bumped(ts, Some(peer))       ⇒ withBumpedPeer(ts, peer)
    case Initialized(_)               ⇒ this
  }

  // lazy val allPeers = activePeers ++ archived

  override def withSnapshot(metadata: SnapshotMetadata, snapshot: Any): DialogRootState = snapshot match {
    case DialogRootStateSnapshot(dialogGroups, _archived, _mobile) ⇒ {
      val state = DialogRootState.initial(userId).copy(
        archived = SortedSet(
          (_archived map (di ⇒ SortableDialog(di.date, di.getPeer))): _*
        )(SortableDialog.OrderingAsc),
        mobile = SortedSet(
          (_mobile map (di ⇒ SortableDialog(di.date, di.getPeer))): _*
        )(SortableDialog.OrderingDesc),
        mobilePeers = _mobile.map(_.getPeer).toSet
      )

      dialogGroups.foldLeft(state) {
        case (acc, DialogGroup(group, infos)) ⇒
          acc.withDialogsInGroup(group, infos map (_.getPeer))
      }
    }
  }

  override lazy val snapshot: Any = {
    val favourites = DialogGroup(
      DialogGroupType.Favourites,
      active.favourites.toSeq map (peer ⇒ DialogInfo(Some(peer)))
    )

    val groups = DialogGroup(
      DialogGroupType.Groups,
      active.groups.toSeq map (peer ⇒ DialogInfo(Some(peer)))
    )

    val dms = DialogGroup(
      DialogGroupType.DirectMessages,
      active.dms.toSeq map (peer ⇒ DialogInfo(Some(peer)))
    )

    DialogRootStateSnapshot(
      dialogGroups = Seq(favourites, groups, dms),
      archived = archived.toSeq map { sd ⇒ DialogInfo(Some(sd.peer), date = sd.ts) },
      mobile = mobile.toSeq map (sd ⇒ DialogInfo(Some(sd.peer), date = sd.ts))
    )
  }

  private def withBumpedPeer(ts: Instant, peer: Peer): DialogRootState = {
    if (mobile.headOption.exists(sd ⇒ sd.ts == ts || sd.ts.isAfter(ts))) withBumpedPeer(mobile.head.ts.plusMillis(1), peer)
    else if (mobilePeers.contains(peer))
      copy(
        mobile = mobile.filterNot(_.peer == peer) + SortableDialog(ts, peer)
      )
    else this
  }

  private def withNewPeer(ts: Instant, peer: Peer): DialogRootState = {
    if (peer.typ.isPrivate && peer.id == userId) this
    else if (this.mobile.headOption.exists(sd ⇒ sd.ts == ts || sd.ts.isAfter(ts))) withNewPeer(mobile.head.ts.plusMillis(1), peer)
    else {
      val sortableDialog = SortableDialog(ts, peer)

      copy(
        active = this.active.withPeer(peer),
        archived = this.archived.filterNot(_.peer == peer),
        mobile = this.mobile + sortableDialog,
        mobilePeers = this.mobilePeers + peer
      )
    }
  }

  private def withUnarchivedPeer(ts: Instant, peer: Peer): DialogRootState = {
    if (!this.archived.exists(_.peer == peer)) this
    else {
      copy(
        active = this.active.withPeer(peer),
        archived = this.archived.filterNot(_.peer == peer)
      )
    }
  }

  private def withArchivedPeer(ts: Instant, peer: Peer): DialogRootState = {
    if ((peer.typ.isPrivate && peer.id == userId) || archived.exists(_.peer == peer)) this
    else if (archived.headOption.exists(sd ⇒ sd.ts == ts || sd.ts.isAfter(ts))) withArchivedPeer(archived.head.ts.plusMillis(1), peer)
    else {
      val sortableDialog = SortableDialog(ts, peer)
      copy(
        active = this.active.withoutPeer(peer),
        archived = this.archived + sortableDialog
      )
    }
  }

  private def withFavouritedPeer(ts: Instant, peer: Peer): DialogRootState = {
    copy(
      active = this.active.withFavouritedPeer(peer),
      archived = this.archived.filterNot(_.peer == peer)
    )
  }

  private def withUnfavouritedPeer(ts: Instant, peer: Peer) = {
    copy(
      active = this.active.withUnfavouritedPeer(peer)
    )
  }

  private def withDeletedPeer(ts: Instant, peer: Peer) = {
    copy(
      active = this.active.withDeletedPeer(peer),
      archived = this.archived.filterNot(_.peer == peer),
      mobile = this.mobile.filterNot(_.peer == peer),
      mobilePeers = this.mobilePeers - peer
    )
  }

  private def withDialogsInGroup(group: DialogGroupType, peers: Seq[Peer]) = {
    val newActive = group match {
      case DialogGroupType.Favourites     ⇒ active.copy(favourites = active.favourites ++ peers)
      case DialogGroupType.Groups         ⇒ active.copy(groups = active.groups ++ peers)
      case DialogGroupType.DirectMessages ⇒ active.copy(dms = active.dms ++ peers)
      case unknown                        ⇒ throw DialogErrors.UnknownDialogGroupType(unknown)
    }

    copy(active = newActive)
  }
}