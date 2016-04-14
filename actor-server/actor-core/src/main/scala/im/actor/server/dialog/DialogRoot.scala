package im.actor.server.dialog

import java.time.Instant

import akka.actor.{ ActorRef, Props, Status }
import akka.pattern.{ ask, pipe }
import akka.util.Timeout
import im.actor.concurrent._
import im.actor.server.cqrs._
import im.actor.server.dialog.DialogCommands.SendMessage
import im.actor.server.model.{ Peer, PeerType }
import im.actor.api.rpc._
import im.actor.api.rpc.messaging.UpdateChatGroupsChanged
import im.actor.api.rpc.misc.ApiExtension
import im.actor.config.ActorConfig
import im.actor.server.dialog.DialogQueries.GetInfoResponse
import im.actor.server.sequence.{ PushRules, SeqState, SeqUpdatesExtension }

import scala.concurrent.Future

trait DialogRootEvent extends TaggedEvent {
  val ts: Instant

  override def tags: Set[String] = Set("dialogRoot")
}

trait DialogRootCommand

trait DialogRootQuery

final case class DialogRootState(
  active:      Map[DialogGroupType, Set[Peer]],
  activePeers: Set[Peer],
  archived:    Set[Peer]
) extends ProcessorState[DialogRootState, DialogRootEvent] {
  import DialogRootEvents._

  override def updated(e: DialogRootEvent): DialogRootState = e match {
    case Created(_, peer)      ⇒ withShownPeer(peer)
    case Archived(_, peer)     ⇒ withArchivedPeer(peer)
    case Unarchived(_, peer)   ⇒ withShownPeer(peer)
    case Favourited(_, peer)   ⇒ withFavouritedPeer(peer)
    case Unfavourited(_, peer) ⇒ withUnfavouritedPeer(peer)
  }

  private def withShownPeer(peer: Peer) =
    copy(
      activePeers = this.activePeers + peer,
      active = this.active + dialogGroup(peer),
      archived = this.archived - peer
    )

  private def withArchivedPeer(peer: Peer) =
    copy(
      activePeers = this.activePeers - peer,
      active = this.active mapValues (_ - peer),
      archived = this.archived + peer
    )

  private def withFavouritedPeer(peer: Peer) =
    copy(
      activePeers = this.activePeers + peer,
      active = this.active.mapValues(_.filterNot(_ == peer)) + dialogGroup(peer, isFavourite = true),
      archived = this.archived - peer
    )

  private def withUnfavouritedPeer(peer: Peer) =
    copy(
      active =
        (this.active.mapValues(_.filterNot(_ == peer)) + dialogGroup(peer)).filter {
          case (DialogGroupType.Favourites, peers) if peers.isEmpty ⇒ false
          case _ ⇒ true
        }
    )

  private def dialogGroup(peer: Peer, isFavourite: Boolean = false) = {
    val group = (isFavourite, peer.typ) match {
      case (true, _)                 ⇒ DialogGroupType.Favourites
      case (false, PeerType.Private) ⇒ DialogGroupType.DirectMessages
      case (false, PeerType.Group)   ⇒ DialogGroupType.Groups
      case _                         ⇒ throw new RuntimeException("Unknown peer type")
    }

    group → (this.active.getOrElse(group, Set.empty) + peer)
  }
}

object DialogRoot {
  def props(userId: Int, extensions: Seq[ApiExtension]) = Props(classOf[DialogRoot], userId, extensions)
}

private class DialogRoot(userId: Int, extensions: Seq[ApiExtension]) extends Processor[DialogRootState, DialogRootEvent] {
  import DialogRootEvents._
  import DialogRootQueries._
  import DialogRootCommands._
  import context.dispatcher

  private implicit val timeout = Timeout(ActorConfig.defaultTimeout)

  private val selfPeer: Peer = Peer.privat(userId)

  override def persistenceId: String = s"DialogRoot_$userId"

  override protected def getInitialState: DialogRootState = DialogRootState(Map.empty, Set.empty, Set.empty)

  override protected def handleQuery: PartialFunction[Any, Future[Any]] = {
    case GetCounter() ⇒
      val refs = state.activePeers.toSeq map (peer ⇒ peer → dialogRef(peer))

      for {
        counters ← FutureExt.ftraverse(refs) {
          case (peer, ref) ⇒
            (ref ? DialogQueries.GetCounter(peer)).mapTo[DialogQueries.GetCounterResponse] map (_.counter)
        }
      } yield GetCounterResponse(counters.sum)
    case GetDialogGroups() ⇒
      fetchDialogGroups() map (GetDialogGroupsResponse(_))
  }

  override protected def handleCommand: Receive = {
    case sm: SendMessage ⇒
      needShowDialog(sm) match {
        case Some(peer) ⇒
          val e = if (isArchived(peer)) Unarchived(Instant.now(), peer) else Created(Instant.now(), peer)

          persist(e) { _ ⇒
            commit(e)
            handleDialogCommand(sm)
            sendChatGroupsChanged()
          }
        case None ⇒
          handleDialogCommand(sm)
      }
    case Archive(peer, clientAuthSid)     ⇒ archive(peer, clientAuthSid)
    case Unarchive(peer, clientAuthSid)   ⇒ unarchive(peer, clientAuthSid)
    case Favourite(peer, clientAuthSid)   ⇒ favourite(peer, clientAuthSid)
    case Unfavourite(peer, clientAuthSid) ⇒ unfavourite(peer, clientAuthSid)
    case dc: DialogCommand                ⇒ handleDialogCommand(dc)
    case dq: DialogQuery                  ⇒ handleDialogQuery(dq)
  }

  def handleDialogCommand: PartialFunction[DialogCommand, Unit] = {
    case ddc: DirectDialogCommand ⇒ dialogRef(ddc) forward ddc
    case dc: DialogCommand        ⇒ dialogRef(dc.dest) forward dc
  }

  def handleDialogQuery: PartialFunction[DialogQuery, Unit] = {
    case dq: DialogQuery ⇒ dialogRef(dq.dest) forward dq
  }

  private def archive(peer: Peer, clientAuthSid: Option[Int]) = {
    if (isArchived(peer)) sender() ! Status.Failure(DialogErrors.DialogAlreadyArchived(peer))
    else persist(Archived(Instant.now(), peer)) { e ⇒
      commit(e)
      sendChatGroupsChanged(clientAuthSid) pipeTo sender()
    }
  }

  private def unarchive(peer: Peer, clientAuthSid: Option[Int]) = {
    if (!isArchived(peer)) sender() ! Status.Failure(DialogErrors.DialogAlreadyShown(peer))
    else persist(Unarchived(Instant.now(), peer)) { e ⇒
      commit(e)
      sendChatGroupsChanged(clientAuthSid) pipeTo sender()
    }
  }

  private def favourite(peer: Peer, clientAuthSid: Option[Int]) = {
    if (isFavourited(peer)) sender() ! Status.Failure(DialogErrors.DialogAlreadyFavourited(peer))
    else persist(Favourited(Instant.now(), peer)) { e ⇒
      commit(e)
      sendChatGroupsChanged(clientAuthSid) pipeTo sender()
    }
  }

  private def unfavourite(peer: Peer, clientAuthSid: Option[Int]) = {
    if (!isFavourited(peer)) sender() ! Status.Failure(DialogErrors.DialogAlreadyUnfavourited(peer))
    else persist(Unfavourited(Instant.now(), peer)) { e ⇒
      commit(e)
      sendChatGroupsChanged(clientAuthSid) pipeTo sender()
    }
  }

  private def needShowDialog(sm: SendMessage): Option[Peer] = {
    val checkPeer =
      sm.origin.typ match {
        case PeerType.Group ⇒ sm.dest
        case PeerType.Private ⇒
          if (selfPeer == sm.dest) sm.origin
          else sm.dest
        case _ ⇒ throw new RuntimeException("Unknown peer type")
      }

    if (dialogShown(checkPeer)) None
    else Some(checkPeer)
  }

  private def isArchived(peer: Peer): Boolean = state.archived.contains(peer)

  private def isFavourited(peer: Peer): Boolean = state.active.get(DialogGroupType.Favourites).exists(_.contains(peer))

  private def dialogShown(peer: Peer): Boolean = state.activePeers.contains(peer)

  private def dialogRef(dc: DirectDialogCommand): ActorRef = {
    val peer = dc.dest match {
      case Peer(PeerType.Group, _)   ⇒ dc.dest
      case Peer(PeerType.Private, _) ⇒ if (dc.origin == selfPeer) dc.dest else dc.origin
    }
    dialogRef(peer)
  }

  private def dialogRef(peer: Peer): ActorRef =
    context.child(dialogName(peer)) getOrElse context.actorOf(DialogProcessor.props(userId, peer, extensions), dialogName(peer))

  private def dialogName(peer: Peer): String = peer.typ match {
    case PeerType.Private ⇒ s"Private_${peer.id}"
    case PeerType.Group   ⇒ s"Group_${peer.id}"
    case other            ⇒ throw new Exception(s"Unknown peer type: $other")
  }

  private def fetchDialogGroups(): Future[Seq[DialogGroup]] = {
    val infosFutures =
      state.active map {
        case (group, peers) ⇒
          FutureExt.ftraverse(peers.toSeq)(peer ⇒ dialogRef(peer) ? DialogQueries.GetInfo(peer))
            .mapTo[Seq[GetInfoResponse]]
            .map(infos ⇒ DialogGroup(group, infos.map(_.info)))
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
}