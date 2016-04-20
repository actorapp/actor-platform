package im.actor.server.dialog

import java.time.Instant

import akka.actor.{ ActorRef, Props, Status }
import akka.pattern.{ ask, pipe }
import akka.persistence.SnapshotMetadata
import akka.util.Timeout
import im.actor.concurrent._
import im.actor.server.cqrs._
import im.actor.server.dialog.DialogCommands.{ SendMessage, WriteMessageSelf }
import im.actor.server.model.{ Peer, PeerType }
import im.actor.api.rpc._
import im.actor.api.rpc.messaging.UpdateChatGroupsChanged
import im.actor.api.rpc.misc.ApiExtension
import im.actor.config.ActorConfig
import im.actor.serialization.ActorSerializer
import im.actor.server.dialog.DialogQueries.GetInfoResponse
import im.actor.server.sequence.{ PushRules, SeqState, SeqUpdatesExtension }

import scala.collection.SortedSet
import scala.concurrent.Future

trait DialogRootEvent extends TaggedEvent {
  val ts: Instant

  override def tags: Set[String] = Set("dialogRoot")
}

trait DialogRootCommand

trait DialogRootQuery

private object SortableDialog {
  val ordering = new Ordering[SortableDialog] {
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
    Map(
      DialogGroupType.DirectMessages → SortedSet.empty(SortableDialog.ordering),
      DialogGroupType.Groups → SortedSet.empty(SortableDialog.ordering)
    ),
    SortedSet.empty(SortableDialog.ordering),
    SortedSet.empty(SortableDialog.ordering)
  )
}

private final case class DialogRootState(
  active:      Map[DialogGroupType, SortedSet[SortableDialog]],
  activePeers: SortedSet[SortableDialog],
  archived:    SortedSet[SortableDialog]
) extends ProcessorState[DialogRootState, DialogRootEvent] {
  import DialogRootEvents._

  override def updated(e: DialogRootEvent): DialogRootState = e match {
    case Created(ts, Some(peer))      ⇒ withShownPeer(ts, peer)
    case Archived(ts, Some(peer))     ⇒ withArchivedPeer(ts, peer)
    case Unarchived(ts, Some(peer))   ⇒ withShownPeer(ts, peer)
    case Favourited(ts, Some(peer))   ⇒ withFavouritedPeer(ts, peer)
    case Unfavourited(ts, Some(peer)) ⇒ withUnfavouritedPeer(ts, peer)
  }

  override def withSnapshot(metadata: SnapshotMetadata, snapshot: Any): DialogRootState = snapshot match {
    case DialogRootStateSnapshot(dialogGroups, _archived) ⇒ {
      val state = DialogRootState.initial.copy(
        archived = SortedSet(
          (_archived map (di ⇒ SortableDialog(di.date, di.getPeer))): _*
        )(SortableDialog.ordering)
      )

      dialogGroups.foldLeft(state) {
        case (acc, DialogGroup(group, infos)) ⇒
          withDialogsInGroup(group, infos map (di ⇒ SortableDialog(di.date, di.getPeer)))
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

  private def withArchivedPeer(ts: Instant, peer: Peer) = {
    val sortableDialog = SortableDialog(ts, peer)

    copy(
      activePeers = this.activePeers - sortableDialog,
      active = this.active mapValues (_ - sortableDialog),
      archived = this.archived + sortableDialog
    )
  }

  private def withFavouritedPeer(ts: Instant, peer: Peer) = {
    val sortableDialog = SortableDialog(ts, peer)

    copy(
      activePeers = this.activePeers + sortableDialog,
      active = this.active.mapValues(_.filterNot(_ == peer)) + dialogGroup(sortableDialog, isFavourite = true),
      archived = this.archived - sortableDialog
    )
  }

  private def withUnfavouritedPeer(ts: Instant, peer: Peer) = {
    val sortableDialog = SortableDialog(ts, peer)

    copy(
      active =
        (this.active.mapValues(_.filterNot(_ == peer)) + dialogGroup(sortableDialog)).filter {
          case (DialogGroupType.Favourites, peers) if peers.isEmpty ⇒ false
          case _ ⇒ true
        }
    )
  }

  private def withDialogsInGroup(group: DialogGroupType, sortableDialogs: Seq[SortableDialog]) =
    copy(
      active = this.active map {
      case (`group`, dialogs) ⇒ (group, dialogs ++ sortableDialogs)
      case other              ⇒ other
    },
      activePeers = this.activePeers ++ sortableDialogs
    )

  private def dialogGroup(sortableDialog: SortableDialog, isFavourite: Boolean = false) = {
    val group = (isFavourite, sortableDialog.peer.typ) match {
      case (true, _)                 ⇒ DialogGroupType.Favourites
      case (false, PeerType.Private) ⇒ DialogGroupType.DirectMessages
      case (false, PeerType.Group)   ⇒ DialogGroupType.Groups
      case _                         ⇒ throw new RuntimeException("Unknown peer type")
    }

    group → (this.active.getOrElse(group, SortedSet.empty(SortableDialog.ordering)) + sortableDialog)
  }
}

object DialogRoot {
  private[dialog] def register() = {
    ActorSerializer.register(
      45010 → classOf[DialogRootEvents.Archived],
      45011 → classOf[DialogRootEvents.Created],
      45012 → classOf[DialogRootEvents.Favourited],
      45013 → classOf[DialogRootEvents.Shown],
      45014 → classOf[DialogRootEvents.Unarchived],
      45015 → classOf[DialogRootEvents.Unfavourited],
      45016 → classOf[DialogRootStateSnapshot]
    )
  }

  def props(userId: Int, extensions: Seq[ApiExtension]) = Props(classOf[DialogRoot], userId, extensions)
}

private trait DialogRootQueryHandlers {
  this: DialogRoot ⇒
  import DialogRootQueries._

  import context._

  private implicit val timeout = Timeout(ActorConfig.defaultTimeout)

  def getDialogs(endDate: Instant, limit: Int): Future[GetDialogsResponse] = {
    val dialogs =
      endDateTimeFrom(endDate) match {
        case Some(_) ⇒ state.activePeers.view.filter(sd ⇒ sd.ts.isBefore(endDate) || sd.ts == endDate).take(limit)
        case None    ⇒ state.activePeers.takeRight(limit)
      }

    for {
      infoss ← Future.sequence(dialogs map (sd ⇒ getInfo(sd.peer) map (sd.ts.toEpochMilli → _.getInfo)))
    } yield GetDialogsResponse(infoss.toMap)
  }

  def getDialogGroups(): Future[GetDialogGroupsResponse] =
    fetchDialogGroups() map (GetDialogGroupsResponse(_))

  def getCounter(): Future[GetCounterResponse] = {
    val refs = state.activePeers.toSeq map (sd ⇒ sd.peer → dialogRef(sd.peer))

    for {
      counters ← FutureExt.ftraverse(refs) {
        case (peer, ref) ⇒
          (ref ? DialogQueries.GetCounter(Some(peer))).mapTo[DialogQueries.GetCounterResponse] map (_.counter)
      }
    } yield GetCounterResponse(counters.sum)
  }

  private def endDateTimeFrom(date: Instant): Option[Instant] = {
    if (date.toEpochMilli == 0l)
      None
    else
      Some(date)
  }
}

private class DialogRoot(userId: Int, extensions: Seq[ApiExtension])
  extends Processor[DialogRootState, DialogRootEvent]
  with IncrementalSnapshots[DialogRootState, DialogRootEvent]
  with DialogRootQueryHandlers {
  import DialogRootEvents._
  import DialogRootQueries._
  import DialogRootCommands._
  import context.dispatcher

  private implicit val timeout = Timeout(ActorConfig.defaultTimeout)

  private val selfPeer: Peer = Peer.privat(userId)

  override def persistenceId: String = s"DialogRoot_$userId"

  override protected def getInitialState: DialogRootState = DialogRootState.initial

  override protected def handleQuery: PartialFunction[Any, Future[Any]] = {
    case GetCounter()               ⇒ getCounter()
    case GetDialogGroups()          ⇒ getDialogGroups()
    case GetDialogs(endDate, limit) ⇒ getDialogs(endDate, limit)
  }

  override protected def handleCommand: Receive = {
    case dc: DialogCommand if dc.isInstanceOf[SendMessage] || dc.isInstanceOf[WriteMessageSelf] ⇒
      needShowDialog(dc) match {
        case Some(peer) ⇒
          val e = if (isArchived(peer)) Unarchived(Instant.now(), Some(peer)) else Created(Instant.now(), Some(peer))

          persist(e) { _ ⇒
            commit(e)
            handleDialogCommand(dc)
            sendChatGroupsChanged()
          }
        case None ⇒
          handleDialogCommand(dc)
      }
    case Archive(Some(peer), clientAuthSid)     ⇒ archive(peer, clientAuthSid map (_.value))
    case Unarchive(Some(peer), clientAuthSid)   ⇒ unarchive(peer, clientAuthSid map (_.value))
    case Favourite(Some(peer), clientAuthSid)   ⇒ favourite(peer, clientAuthSid map (_.value))
    case Unfavourite(Some(peer), clientAuthSid) ⇒ unfavourite(peer, clientAuthSid map (_.value))
    case dc: DialogCommand                      ⇒ handleDialogCommand(dc)
    case dq: DialogQuery                        ⇒ handleDialogQuery(dq)
  }

  def handleDialogCommand: PartialFunction[DialogCommand, Unit] = {
    case ddc: DirectDialogCommand ⇒ dialogRef(ddc) forward ddc
    case dc: DialogCommand        ⇒ dialogRef(dc.getDest) forward dc
  }

  def handleDialogQuery: PartialFunction[DialogQuery, Unit] = {
    case dq: DialogQuery ⇒ dialogRef(dq.getDest) forward dq
  }

  private def archive(peer: Peer, clientAuthSid: Option[Int]) = {
    if (isArchived(peer)) sender() ! Status.Failure(DialogErrors.DialogAlreadyArchived(peer))
    else persist(Archived(Instant.now(), Some(peer))) { e ⇒
      commit(e)
      sendChatGroupsChanged(clientAuthSid) pipeTo sender()
    }
  }

  private def unarchive(peer: Peer, clientAuthSid: Option[Int]) = {
    if (!isArchived(peer)) sender() ! Status.Failure(DialogErrors.DialogAlreadyShown(peer))
    else persist(Unarchived(Instant.now(), Some(peer))) { e ⇒
      commit(e)
      sendChatGroupsChanged(clientAuthSid) pipeTo sender()
    }
  }

  private def favourite(peer: Peer, clientAuthSid: Option[Int]) = {
    if (isFavourited(peer)) sender() ! Status.Failure(DialogErrors.DialogAlreadyFavourited(peer))
    else persist(Favourited(Instant.now(), Some(peer))) { e ⇒
      commit(e)
      sendChatGroupsChanged(clientAuthSid) pipeTo sender()
    }
  }

  private def unfavourite(peer: Peer, clientAuthSid: Option[Int]) = {
    if (!isFavourited(peer)) sender() ! Status.Failure(DialogErrors.DialogAlreadyUnfavourited(peer))
    else persist(Unfavourited(Instant.now(), Some(peer))) { e ⇒
      commit(e)
      sendChatGroupsChanged(clientAuthSid) pipeTo sender()
    }
  }

  private def needShowDialog(cmd: DialogCommand): Option[Peer] = {
    val checkPeerOpt = cmd match {
      case sm: SendMessage ⇒
        Some(sm.getOrigin.typ match {
          case PeerType.Group ⇒ sm.getDest
          case PeerType.Private ⇒
            if (selfPeer == sm.getDest) sm.getOrigin
            else sm.getDest
          case _ ⇒ throw new RuntimeException("Unknown peer type")
        })
      case wm: WriteMessageSelf ⇒ Some(wm.getDest)
      case _                    ⇒ None
    }

    checkPeerOpt flatMap { checkPeer ⇒
      if (dialogShown(checkPeer)) None
      else Some(checkPeer)
    }
  }

  private def isArchived(peer: Peer): Boolean = state.archived.contains(SortableDialog(Instant.MIN, peer))

  private def isFavourited(peer: Peer): Boolean = state.active.get(DialogGroupType.Favourites).exists(_.contains(SortableDialog(Instant.MIN, peer)))

  private def dialogShown(peer: Peer): Boolean = state.activePeers.contains(SortableDialog(Instant.MIN, peer))

  protected def dialogRef(dc: DirectDialogCommand): ActorRef = {
    val peer = dc.getDest match {
      case Peer(PeerType.Group, _)   ⇒ dc.getDest
      case Peer(PeerType.Private, _) ⇒ if (dc.getOrigin == selfPeer) dc.getDest else dc.getOrigin
    }
    dialogRef(peer)
  }

  protected def dialogRef(peer: Peer): ActorRef =
    context.child(dialogName(peer)) getOrElse context.actorOf(DialogProcessor.props(userId, peer, extensions), dialogName(peer))

  private def dialogName(peer: Peer): String = peer.typ match {
    case PeerType.Private ⇒ s"Private_${peer.id}"
    case PeerType.Group   ⇒ s"Group_${peer.id}"
    case other            ⇒ throw new Exception(s"Unknown peer type: $other")
  }

  protected def fetchDialogGroups(): Future[Seq[DialogGroup]] = {
    val infosFutures =
      state.active map {
        case (group, sortableDialogs) ⇒
          FutureExt.ftraverse(sortableDialogs.toSeq)(sd ⇒ getInfo(sd.peer))
            .map(infos ⇒ DialogGroup(group, infos.map(_.getInfo)))
      }

    Future.sequence(infosFutures) map (_.toSeq)
  }

  private def sendChatGroupsChanged(ignoreAuthSid: Option[Int] = None): Future[SeqState] = {
    for {
      groups ← DialogExtension(context.system).fetchApiGroupedDialogs(userId)
      update = UpdateChatGroupsChanged(groups)
      seqstate ← SeqUpdatesExtension(context.system).
        deliverSingleUpdate(userId, update, PushRules().withExcludeAuthSids(ignoreAuthSid.toSeq))
    } yield seqstate
  }

  protected def getInfo(peer: Peer): Future[DialogQueries.GetInfoResponse] =
    (dialogRef(peer) ? DialogQueries.GetInfo(Some(peer))).mapTo[GetInfoResponse]
}