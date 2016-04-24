package im.actor.server.dialog

import java.time.Instant

import akka.actor.{ ActorRef, Props, Status }
import akka.pattern.{ ask, pipe }
import akka.util.Timeout
import com.google.protobuf.wrappers.Int64Value
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

import scala.concurrent.Future

object DialogRoot {
  private[dialog] def register() = {
    ActorSerializer.register(
      45010 → classOf[DialogRootEvents.Archived],
      45011 → classOf[DialogRootEvents.Created],
      45012 → classOf[DialogRootEvents.Favourited],
      45013 → classOf[DialogRootEvents.Shown],
      45014 → classOf[DialogRootEvents.Unarchived],
      45015 → classOf[DialogRootEvents.Unfavourited],
      45017 → classOf[DialogRootEvents.Initialized],
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
        case Some(_) ⇒ state.allPeers.view.filter(sd ⇒ sd.ts.isBefore(endDate) || sd.ts == endDate).take(limit)
        case None    ⇒ state.allPeers.takeRight(limit)
      }

    for {
      infos ← Future.sequence(dialogs map (sd ⇒ getInfo(sd.peer) map (sd.ts.toEpochMilli → _.getInfo)))
    } yield GetDialogsResponse(infos.toMap)
  }

  def getArchivedDialogs(offsetOpt: Option[Int64Value], limit: Int): Future[GetArchivedDialogsResponse] = {
    val dialogs = (offsetOpt.map(offset ⇒ Instant.ofEpochMilli(offset.value)) match {
      case None         ⇒ state.archived
      case Some(offset) ⇒ state.archived.dropWhile(sd ⇒ sd.ts.isAfter(offset) || sd.ts == offset)
    }).take(limit)

    for {
      infos ← Future.sequence(dialogs map (sd ⇒ getInfo(sd.peer) map (sd.ts.toEpochMilli → _.getInfo)))
    } yield GetArchivedDialogsResponse(
      dialogs = infos.toMap,
      nextOffset = infos.lastOption map (tup ⇒ Int64Value(tup._1))
    )
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

private class DialogRoot(val userId: Int, extensions: Seq[ApiExtension])
  extends Processor[DialogRootState]
  with IncrementalSnapshots[DialogRootState]
  with DialogRootQueryHandlers
  with DialogRootMigration {
  import DialogRootEvents._
  import DialogRootQueries._
  import DialogRootCommands._
  import context.dispatcher

  private implicit val timeout = Timeout(ActorConfig.defaultTimeout)

  private val selfPeer: Peer = Peer.privat(userId)

  override def persistenceId: String = s"DialogRoot_$userId"

  override protected def getInitialState: DialogRootState = DialogRootState.initial

  override protected def handleQuery: PartialFunction[Any, Future[Any]] = {
    case GetCounter()                      ⇒ getCounter()
    case GetDialogGroups()                 ⇒ getDialogGroups()
    case GetDialogs(endDate, limit)        ⇒ getDialogs(endDate, limit)
    case GetArchivedDialogs(offset, limit) ⇒ getArchivedDialogs(offset, limit)
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

  private def isArchived(peer: Peer): Boolean = state.archived.exists(_.peer == peer)

  private def isFavourited(peer: Peer): Boolean = state.active.favourites.exists(_.peer == peer)

  private def dialogShown(peer: Peer): Boolean = state.activePeers.exists(_.peer == peer)

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
    for {
      favInfos ← Future.sequence(state.active.favourites map (sd ⇒ getInfo(sd.peer) map (_.getInfo)))
      groupInfos ← Future.sequence(state.active.groups map (sd ⇒ getInfo(sd.peer) map (_.getInfo)))
      dmInfos ← Future.sequence(state.active.dms map (sd ⇒ getInfo(sd.peer) map (_.getInfo)))
    } yield {
      val base = List(
        DialogGroup(DialogGroupType.Groups, groupInfos.toSeq),
        DialogGroup(DialogGroupType.DirectMessages, dmInfos.toSeq)
      )

      if (favInfos.nonEmpty) DialogGroup(DialogGroupType.Favourites, favInfos.toSeq) :: base
      else base
    }
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