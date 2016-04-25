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
    SortedSet.empty(SortableDialog.OrderingAsc),
    SortedSet.empty(SortableDialog.OrderingAsc),
    SortedSet.empty(SortableDialog.OrderingAsc)
  )
}

private[dialog] case class ActiveDialogs(
  favourites: SortedSet[SortableDialog],
  groups:     SortedSet[SortableDialog],
  dms:        SortedSet[SortableDialog]
) {
  def withPeer(sd: SortableDialog) = {
    if (favourites.exists(_.peer == sd.peer)) this
    else
      sd.peer.typ match {
        case PeerType.Private ⇒ copy(dms = dms + sd)
        case PeerType.Group   ⇒ copy(groups = groups + sd)
        case unknown          ⇒ throw new PeerErrors.UnknownPeerType(unknown)
      }
  }

  def withoutPeer(sd: SortableDialog) = {
    sd.peer.typ match {
      case PeerType.Private ⇒ copy(dms = dms.filterNot(_.peer == sd.peer), favourites = favourites.filterNot(_.peer == sd.peer))
      case PeerType.Group   ⇒ copy(groups = groups.filterNot(_.peer == sd.peer), favourites = favourites.filterNot(_.peer == sd.peer))
      case unknown          ⇒ throw PeerErrors.UnknownPeerType(unknown)
    }
  }

  def withFavouritedPeer(sd: SortableDialog) = {
    sd.peer.typ match {
      case PeerType.Private ⇒ copy(dms = dms.filterNot(_.peer == sd.peer), favourites = favourites + sd)
      case PeerType.Group   ⇒ copy(groups = groups.filterNot(_.peer == sd.peer), favourites = favourites + sd)
      case unknown          ⇒ throw PeerErrors.UnknownPeerType(unknown)
    }
  }

  def withUnfavouritedPeer(sd: SortableDialog) = {
    sd.peer.typ match {
      case PeerType.Private ⇒ copy(dms = dms + sd, favourites = favourites.filterNot(_.peer == sd.peer))
      case PeerType.Group   ⇒ copy(groups = groups + sd, favourites = favourites.filterNot(_.peer == sd.peer))
      case unknown          ⇒ throw PeerErrors.UnknownPeerType(unknown)
    }
  }

  def exists(f: SortableDialog ⇒ Boolean) = favourites.exists(f) || groups.exists(f) || dms.exists(f)

  def map[A](f: SortableDialog ⇒ A) = favourites.map(f) ++ groups.map(f) ++ dms.map(f)

  def find(f: SortableDialog ⇒ Boolean) = favourites.find(f).getOrElse(groups.find(f).getOrElse(dms.find(f)))
}

private object DialogRootState {
  def initial(userId: Int) = DialogRootState(
    userId = userId,
    active = ActiveDialogs.empty,
    // activePeers = SortedSet.empty(SortableDialog.OrderingAsc),
    mobile = SortedSet.empty(SortableDialog.OrderingDesc),
    archived = SortedSet.empty(SortableDialog.OrderingDesc)
  )
}

private[dialog] final case class DialogRootState(
  userId: Int,
  active: ActiveDialogs,
  // activePeers: SortedSet[SortableDialog],
  mobile:   SortedSet[SortableDialog],
  archived: SortedSet[SortableDialog]
) extends ProcessorState[DialogRootState] {
  import DialogRootEvents._

  override def updated(e: Event): DialogRootState = e match {
    case Created(ts, Some(peer))      ⇒ withNewPeer(ts, peer)
    case Archived(ts, Some(peer))     ⇒ withArchivedPeer(ts, peer)
    case Unarchived(ts, Some(peer))   ⇒ withUnarchivedPeer(ts, peer)
    case Favourited(ts, Some(peer))   ⇒ withFavouritedPeer(ts, peer)
    case Unfavourited(ts, Some(peer)) ⇒ withUnfavouritedPeer(ts, peer)
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
        )(SortableDialog.OrderingDesc)
      )

      dialogGroups.foldLeft(state) {
        case (acc, DialogGroup(group, infos)) ⇒
          acc.withDialogsInGroup(group, infos map (di ⇒ SortableDialog(di.date, di.getPeer)))
      }
    }
  }

  override lazy val snapshot: Any = {
    val favourites = DialogGroup(
      DialogGroupType.Favourites,
      active.favourites.toSeq map (sd ⇒ DialogInfo(Some(sd.peer), date = sd.ts))
    )

    val groups = DialogGroup(
      DialogGroupType.Groups,
      active.groups.toSeq map (sd ⇒ DialogInfo(Some(sd.peer), date = sd.ts))
    )

    val dms = DialogGroup(
      DialogGroupType.DirectMessages,
      active.dms.toSeq map (sd ⇒ DialogInfo(Some(sd.peer), date = sd.ts))
    )

    DialogRootStateSnapshot(
      dialogGroups = Seq(favourites, groups, dms),
      archived = archived.toSeq map { sd ⇒ DialogInfo(Some(sd.peer), date = sd.ts) },
      mobile = mobile.toSeq map (sd ⇒ DialogInfo(Some(sd.peer), date = sd.ts))
    )
  }

  private def withBumpedPeer(ts: Instant, peer: Peer): DialogRootState = {
    if (mobile.exists(_.peer == peer))
      copy(
        mobile = mobile.filterNot(_.peer == peer) + SortableDialog(ts, peer)
      )
    else this
  }

  private def withNewPeer(ts: Instant, peer: Peer): DialogRootState = {
    if (peer.typ.isPrivate && peer.id == userId) this
    else if (this.mobile.exists(_.ts == ts)) withNewPeer(ts.plusMillis(1), peer)
    else {
      val sortableDialog = SortableDialog(ts, peer)

      copy(
        active = this.active.withPeer(sortableDialog),
        archived = this.archived.filterNot(_.peer == peer),
        mobile = this.mobile + sortableDialog
      )
    }
  }

  private def withUnarchivedPeer(ts: Instant, peer: Peer): DialogRootState = {
    if (!this.archived.exists(_.peer == peer)) this
    else if (this.active.exists(_.ts == ts)) withUnarchivedPeer(ts.plusMillis(1), peer)
    else {
      val sortableDialog = SortableDialog(ts, peer)

      copy(
        active = this.active.withPeer(sortableDialog),
        archived = this.archived.filterNot(_.peer == peer)
      )
    }
  }

  private def withArchivedPeer(ts: Instant, peer: Peer): DialogRootState = {
    if (!this.active.exists(_.peer == peer)) this
    else if (archived.exists(_.ts == ts)) withArchivedPeer(ts.plusMillis(1), peer)
    else {
      val sortableDialog = SortableDialog(ts, peer)
      copy(
        // activePeers = this.activePeers.filterNot(_.peer == peer),
        active = this.active.withoutPeer(sortableDialog),
        archived = this.archived + sortableDialog
      )
    }
  }

  private def withFavouritedPeer(ts: Instant, peer: Peer): DialogRootState = {
    val sortableDialog = SortableDialog(ts, peer)

    if (active.favourites.exists(_.ts == ts)) withFavouritedPeer(ts.plusMillis(1), peer)
    else
      copy(
        // activePeers = this.activePeers + sortableDialog,
        active = this.active.withFavouritedPeer(sortableDialog),
        archived = this.archived.filterNot(_.peer == peer)
      )
  }

  private def withUnfavouritedPeer(ts: Instant, peer: Peer) = {
    val sortableDialog = SortableDialog(ts, peer)

    copy(
      active = this.active.withUnfavouritedPeer(sortableDialog)
    )
  }

  private def withDialogsInGroup(group: DialogGroupType, sortableDialogs: Seq[SortableDialog]) = {
    val newActive = group match {
      case DialogGroupType.Favourites     ⇒ active.copy(favourites = active.favourites ++ sortableDialogs)
      case DialogGroupType.Groups         ⇒ active.copy(groups = active.groups ++ sortableDialogs)
      case DialogGroupType.DirectMessages ⇒ active.copy(dms = active.dms ++ sortableDialogs)
      case unknown                        ⇒ throw DialogErrors.UnknownDialogGroupType(unknown)
    }

    copy(
      active = newActive
    // activePeers = this.activePeers ++ sortableDialogs
    )
  }
}