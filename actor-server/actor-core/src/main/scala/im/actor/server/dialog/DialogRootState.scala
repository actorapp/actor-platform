package im.actor.server.dialog

import java.time.Instant

import akka.persistence.SnapshotMetadata
import im.actor.server.cqrs.{ Event, ProcessorState, TaggedEvent }
import im.actor.server.model.{ Peer, PeerType }

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

private object DialogRootState {
  val initial = DialogRootState(
    active = Map(
      DialogGroupType.Groups → SortedSet.empty(SortableDialog.OrderingAsc),
      DialogGroupType.DirectMessages → SortedSet.empty(SortableDialog.OrderingAsc)
    ),
    activePeers = SortedSet.empty(SortableDialog.OrderingAsc),
    archived = SortedSet.empty(SortableDialog.OrderingDesc)
  )
}

private[dialog] final case class DialogRootState(
  active:      Map[DialogGroupType, SortedSet[SortableDialog]],
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

  override lazy val snapshot: Any = DialogRootStateSnapshot(
    dialogGroups = active.toSeq map {
    case (typ, sortableDialogs) ⇒
      DialogGroup(
        typ,
        sortableDialogs.toSeq map (sd ⇒ DialogInfo(Some(sd.peer), date = sd.ts))
      )
  },
    archived = archived.toSeq map { sd ⇒ DialogInfo(Some(sd.peer), date = sd.ts) }
  )

  private def withShownPeer(ts: Instant, peer: Peer): DialogRootState = {
    val sortableDialog = SortableDialog(ts, peer)

    if (this.activePeers.exists(_.ts == ts)) withShownPeer(ts.plusMillis(1), peer)
    else
      copy(
        activePeers = this.activePeers + sortableDialog,
        active = this.active + dialogGroup(sortableDialog),
        archived = this.archived - sortableDialog
      )
  }

  private def withArchivedPeer(ts: Instant, peer: Peer): DialogRootState = {
    val sortableDialog = SortableDialog(ts, peer)

    if (archived.exists(_.ts == ts)) withArchivedPeer(ts.plusMillis(1), peer)
    else
      copy(
        activePeers = this.activePeers - sortableDialog,
        active = this.active mapValues (_ - sortableDialog),
        archived = this.archived + sortableDialog
      )
  }

  private def withFavouritedPeer(ts: Instant, peer: Peer): DialogRootState = {
    val sortableDialog = SortableDialog(ts, peer)

    if (activePeers.exists(_.ts == ts)) withFavouritedPeer(ts.plusMillis(1), peer)
    else
      copy(
        activePeers = this.activePeers + sortableDialog,
        active = this.active.mapValues(_.filterNot(_.peer == peer)) + dialogGroup(sortableDialog, isFavourite = true),
        archived = this.archived - sortableDialog
      )
  }

  private def withUnfavouritedPeer(ts: Instant, peer: Peer) = {
    val sortableDialog = SortableDialog(ts, peer)

    copy(
      active =
        (this.active.mapValues(_.filterNot(_.peer == peer)) + dialogGroup(sortableDialog)).filter {
          case (DialogGroupType.Favourites, peers) if peers.isEmpty ⇒ false
          case _ ⇒ true
        }
    )
  }

  private def withDialogsInGroup(group: DialogGroupType, sortableDialogs: Seq[SortableDialog]) = {
    val activeBase =
      if (this.active.contains(group)) this.active
      else this.active + (group → SortedSet.empty(SortableDialog.OrderingAsc))

    copy(
      active = activeBase map {
      case (`group`, dialogs) ⇒ (group, dialogs ++ sortableDialogs)
      case other              ⇒ other
    },
      activePeers = this.activePeers ++ sortableDialogs
    )
  }

  private def dialogGroup(sortableDialog: SortableDialog, isFavourite: Boolean = false) = {
    val group = (isFavourite, sortableDialog.peer.typ) match {
      case (true, _)                 ⇒ DialogGroupType.Favourites
      case (false, PeerType.Private) ⇒ DialogGroupType.DirectMessages
      case (false, PeerType.Group)   ⇒ DialogGroupType.Groups
      case _                         ⇒ throw new RuntimeException("Unknown peer type")
    }

    group → (this.active.getOrElse(group, SortedSet.empty(SortableDialog.OrderingAsc)) + sortableDialog)
  }
}