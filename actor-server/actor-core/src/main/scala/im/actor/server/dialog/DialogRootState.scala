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
    override def compare(x: SortableDialog, y: SortableDialog): Int =
      if (x.peer == y.peer) 0
      else if (x.ts.isBefore(y.ts)) -1
      else if (x.ts.isAfter(y.ts)) 1
      else 0
  }

  val OrderingDesc = new Ordering[SortableDialog] {
    override def compare(x: SortableDialog, y: SortableDialog): Int =
      if (x.peer == y.peer) 0
      else if (x.ts.isBefore(y.ts)) 1
      else if (x.ts.isAfter(y.ts)) -1
      else 0
  }
}

private case class SortableDialog(ts: Instant, peer: Peer)

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
    sd.peer.typ match {
      case PeerType.Private ⇒ copy(dms = dms + sd)
      case PeerType.Group   ⇒ copy(groups = groups + sd)
      case unknown          ⇒ throw new PeerErrors.UnknownPeerType(unknown)
    }
  }

  def withoutPeer(sd: SortableDialog) = {
    sd.peer.typ match {
      case PeerType.Private ⇒ copy(dms = dms - sd, favourites = favourites - sd)
      case PeerType.Group   ⇒ copy(groups = groups - sd, favourites = favourites - sd)
      case unknown          ⇒ throw PeerErrors.UnknownPeerType(unknown)
    }
  }

  def withFavouritedPeer(sd: SortableDialog) = {
    sd.peer.typ match {
      case PeerType.Private ⇒ copy(dms = dms - sd, favourites = favourites + sd)
      case PeerType.Group   ⇒ copy(groups = groups - sd, favourites = favourites + sd)
      case unknown          ⇒ throw PeerErrors.UnknownPeerType(unknown)
    }
  }

  def withUnfavouritedPeer(sd: SortableDialog) = {
    sd.peer.typ match {
      case PeerType.Private ⇒ copy(dms = dms + sd, favourites = favourites - sd)
      case PeerType.Group   ⇒ copy(groups = groups + sd, favourites = favourites - sd)
      case unknown          ⇒ throw PeerErrors.UnknownPeerType(unknown)
    }
  }
}

private object DialogRootState {
  val initial = DialogRootState(
    active = ActiveDialogs.empty,
    activePeers = SortedSet.empty(SortableDialog.OrderingAsc),
    archived = SortedSet.empty(SortableDialog.OrderingDesc)
  )
}

private[dialog] final case class DialogRootState(
  active:      ActiveDialogs,
  activePeers: SortedSet[SortableDialog],
  archived:    SortedSet[SortableDialog]
) extends ProcessorState[DialogRootState] {
  import DialogRootEvents._

  override def updated(e: Event): DialogRootState = e match {
    case Created(ts, Some(peer))      ⇒ withShownPeer(ts, peer)
    case Archived(ts, Some(peer))     ⇒ withArchivedPeer(ts, peer)
    case Unarchived(ts, Some(peer))   ⇒ withShownPeer(ts, peer)
    case Favourited(ts, Some(peer))   ⇒ withFavouritedPeer(ts, peer)
    case Unfavourited(ts, Some(peer)) ⇒ withUnfavouritedPeer(ts, peer)
    case Initialized(_)               ⇒ this
  }

  lazy val allPeers = activePeers ++ archived

  override def withSnapshot(metadata: SnapshotMetadata, snapshot: Any): DialogRootState = snapshot match {
    case DialogRootStateSnapshot(dialogGroups, _archived) ⇒ {
      val state = DialogRootState.initial.copy(
        archived = SortedSet(
          (_archived map (di ⇒ SortableDialog(di.date, di.getPeer))): _*
        )(SortableDialog.OrderingAsc)
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
      archived = archived.toSeq map { sd ⇒ DialogInfo(Some(sd.peer), date = sd.ts) }
    )
  }

  private def withShownPeer(ts: Instant, peer: Peer): DialogRootState = {
    val sortableDialog = SortableDialog(ts, peer)

    if (this.activePeers.exists(_.ts == ts)) withShownPeer(ts.plusMillis(1), peer)
    else
      copy(
        activePeers = this.activePeers + sortableDialog,
        active = this.active.withPeer(sortableDialog),
        archived = this.archived - sortableDialog
      )
  }

  private def withArchivedPeer(ts: Instant, peer: Peer): DialogRootState = {
    val sortableDialog = SortableDialog(ts, peer)

    if (archived.exists(_.ts == ts)) withArchivedPeer(ts.plusMillis(1), peer)
    else
      copy(
        activePeers = this.activePeers - sortableDialog,
        active = this.active.withoutPeer(sortableDialog),
        archived = this.archived + sortableDialog
      )
  }

  private def withFavouritedPeer(ts: Instant, peer: Peer): DialogRootState = {
    val sortableDialog = SortableDialog(ts, peer)

    if (activePeers.exists(_.ts == ts)) withFavouritedPeer(ts.plusMillis(1), peer)
    else
      copy(
        activePeers = this.activePeers + sortableDialog,
        active = this.active.withFavouritedPeer(sortableDialog),
        archived = this.archived - sortableDialog
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
      active = newActive,
      activePeers = this.activePeers ++ sortableDialogs
    )
  }
}