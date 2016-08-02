package im.actor.server.dialog

import java.time.{ Instant, Period }

import akka.actor.Props
import akka.http.scaladsl.util.FastFuture
import akka.pattern.{ ask, pipe }
import akka.util.Timeout
import com.google.protobuf.wrappers.Int64Value
import im.actor.api.rpc._
import im.actor.api.rpc.messaging.{ UpdateChatDelete, UpdateChatGroupsChanged }
import im.actor.api.rpc.misc.ApiExtension
import im.actor.concurrent._
import im.actor.config.ActorConfig
import im.actor.serialization.ActorSerializer
import im.actor.server.cqrs._
import im.actor.server.db.DbExtension
import im.actor.server.dialog.DialogCommands.{ SendMessage, WriteMessageSelf }
import im.actor.server.dialog.DialogQueries.GetInfoResponse
import im.actor.server.group.GroupExtension
import im.actor.server.model.{ Peer, PeerErrors, PeerType }
import im.actor.server.persist.HistoryMessageRepo
import im.actor.server.sequence.{ PushRules, SeqState, SeqUpdatesExtension }
import im.actor.server.user.UserExtension

import scala.concurrent.duration._
import scala.concurrent.Future

object DialogRoot {
  private[dialog] def register() = {
    ActorSerializer.register(
      45010 → classOf[DialogRootEvents.Archived],
      45011 → classOf[DialogRootEvents.Created],
      45012 → classOf[DialogRootEvents.Favourited],
      45014 → classOf[DialogRootEvents.Unarchived],
      45015 → classOf[DialogRootEvents.Unfavourited],
      45017 → classOf[DialogRootEvents.Initialized],
      45016 → classOf[DialogRootStateSnapshot],
      45018 → classOf[DialogRootEvents.Bumped],
      45019 → classOf[DialogRootEvents.Deleted]
    )
  }

  def props(userId: Int, extensions: Seq[ApiExtension]) = Props(classOf[DialogRoot], userId, extensions)
}

private trait DialogRootQueryHandlers {
  this: DialogRoot ⇒
  import DialogRootQueries._
  import context._

  private implicit val timeout = Timeout(ActorConfig.defaultTimeout)

  protected def getDialogs(endDate: Instant, limit: Int): Future[GetDialogsResponse] = {
    val dialogs =
      endDateTimeFrom(endDate) match {
        case Some(_) ⇒ state.mobile.view.filter(sd ⇒ sd.ts.isBefore(endDate) || sd.ts == endDate).take(limit)
        case None    ⇒ state.mobile.takeRight(limit)
      }

    for {
      infos ← Future.sequence(dialogs map (sd ⇒ getInfo(sd.peer) map (sd.ts.toEpochMilli → _.getInfo)))
    } yield GetDialogsResponse(infos.toMap)
  }

  protected def getArchivedDialogs(offsetOpt: Option[Long], limit: Int): Future[GetArchivedDialogsResponse] = {
    val dialogs = ((offsetOpt map Instant.ofEpochMilli) match {
      case None         ⇒ state.archived
      case Some(offset) ⇒ state.archived.dropWhile(sd ⇒ sd.ts.isAfter(offset) || sd.ts == offset)
    }).take(limit)

    for {
      infos ← Future.sequence(dialogs map (sd ⇒ getInfo(sd.peer) map (sd.ts.toEpochMilli → _.getInfo)))
    } yield GetArchivedDialogsResponse(
      dialogs = infos.toMap,
      nextOffset = infos.lastOption map (_._1)
    )
  }

  protected def getDialogGroups(): Future[GetDialogGroupsResponse] =
    fetchDialogGroups() map (GetDialogGroupsResponse(_))

  protected def getCounter(): Future[GetCounterResponse] = {
    for {
      counters ← FutureExt.ftraverse(state.active.map(identity).toSeq) { peer ⇒
        (context.parent ? DialogQueries.GetCounter(Some(peer))).mapTo[DialogQueries.GetCounterResponse] map (_.counter)
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
  import DialogRootCommands._
  import DialogRootEvents._
  import DialogRootQueries._
  import context.dispatcher

  private val system = context.system
  private val seqUpdExt = SeqUpdatesExtension(system)
  private val userExt = UserExtension(system)
  private val groupExt = GroupExtension(system)
  private val db = DbExtension(system).db

  private implicit val timeout = Timeout(ActorConfig.defaultTimeout)

  private val archiveCheckDelay = (userId % 60).minutes
  private val archiveCheckInterval = system.scheduler.schedule(archiveCheckDelay, 1.hour, self, CheckArchive())

  override def postStop(): Unit = {
    super.postStop()
    archiveCheckInterval.cancel()
  }

  private val selfPeer: Peer = Peer.privat(userId)

  override def persistenceId: String = s"DialogRoot_$userId"

  override protected def getInitialState: DialogRootState = DialogRootState.initial(userId)

  override protected def handleQuery: PartialFunction[Any, Future[Any]] = {
    case GetCounter()                      ⇒ getCounter()
    case GetDialogGroups()                 ⇒ getDialogGroups()
    case GetDialogs(endDate, limit)        ⇒ getDialogs(endDate, limit)
    case GetArchivedDialogs(offset, limit) ⇒ getArchivedDialogs(offset, limit)
  }

  override protected def handleCommand: Receive = {
    case Bump(Some(peer))                      ⇒ bump(peer)
    case Archive(Some(peer), clientAuthId)     ⇒ archive(peer, clientAuthId)
    case Unarchive(Some(peer), clientAuthId)   ⇒ unarchive(peer, clientAuthId)
    case Favourite(Some(peer), clientAuthId)   ⇒ favourite(peer, clientAuthId)
    case Unfavourite(Some(peer), clientAuthId) ⇒ unfavourite(peer, clientAuthId)
    case Delete(Some(peer), clientAuthId)      ⇒ delete(peer, clientAuthId)
    case CheckArchive()                        ⇒ checkArchive()
  }

  private def checkArchive(): Unit =
    getDialogGroups() foreach { resp ⇒
      val toArchive = resp.groups.collect {
        case DialogGroup(DialogGroupType.DirectMessages, ds) ⇒ ds
        case DialogGroup(DialogGroupType.Groups, ds)         ⇒ ds
      }.flatten filter (d ⇒ d.counter == 0 && d.lastMessageDate.isBefore(Instant.now().minus(Period.ofDays(5))))

      toArchive foreach { d ⇒
        log.debug("Archiving dialog {} due to inactivity. Last message date: {}", d.peer, d.lastMessageDate)
        self ! Archive(d.peer, clientAuthId = 0L)
      }
    }

  private def archive(peer: Peer, clientAuthId: Long) = {
    if (isArchived(peer)) sendChatGroupsChanged(clientAuthId) pipeTo sender()
    else persist(Archived(Instant.now(), Some(peer))) { e ⇒
      commit(e)
      sendChatGroupsChanged(clientAuthId) pipeTo sender()
    }
  }

  private def unarchive(peer: Peer, clientAuthId: Long) = {
    if (!isArchived(peer)) sendChatGroupsChanged(clientAuthId) pipeTo sender()
    else persist(Unarchived(Instant.now(), Some(peer))) { e ⇒
      commit(e)
      sendChatGroupsChanged(clientAuthId) pipeTo sender()
    }
  }

  private def favourite(peer: Peer, clientAuthId: Long) = {
    if (isFavourited(peer)) sendChatGroupsChanged(clientAuthId) pipeTo sender()
    else persist(Favourited(Instant.now(), Some(peer))) { e ⇒
      commit(e)
      sendChatGroupsChanged(clientAuthId) pipeTo sender()
    }
  }

  private def unfavourite(peer: Peer, clientAuthId: Long) = {
    if (!isFavourited(peer)) sendChatGroupsChanged(clientAuthId) pipeTo sender()
    else persist(Unfavourited(Instant.now(), Some(peer))) { e ⇒
      commit(e)
      sendChatGroupsChanged(clientAuthId) pipeTo sender()
    }
  }

  private def delete(peer: Peer, clientAuthId: Long) = {
    if (!dialogExists(peer)) sendChatGroupsChanged(clientAuthId) pipeTo sender()
    else persist(Deleted(Instant.now(), Some(peer))) { e ⇒
      commit(e)
      (for {
        _ ← db.run(HistoryMessageRepo.deleteAll(userId, peer))
        seqState ← seqUpdExt.deliverClientUpdate(userId, clientAuthId, update = UpdateChatDelete(peer.asStruct))
        _ ← sendChatGroupsChanged(0L)
        //        _ = thatDialog ! PoisonPill // kill that dialog would be good
      } yield seqState) pipeTo sender()
    }
  }

  private def bump(peer: Peer) = {
    val now = Instant.now

    val isCreated = isDialogCreated(peer)
    val isShown = isDialogShown(peer)

    val createdEvt = if (!isCreated) Some(Created(now).withPeer(peer)) else None
    val shownEvt = if (isCreated && !isShown) Some(Unarchived(now).withPeer(peer)) else None
    val bumpedEvt = if (isCreated && !isDialogOnTop(peer)) Some(Bumped(now).withPeer(peer)) else None

    val events: List[DialogRootEvent] = (createdEvt ++ shownEvt ++ bumpedEvt).toList

    persistAllAsync(events)(e ⇒ commit(e))

    val replyTo = sender()
    deferAsync(()) { _ ⇒
      (if (!isCreated || !isShown)
        sendChatGroupsChanged(0L) map (_ ⇒ BumpAck())
      else
        FastFuture.successful(BumpAck())) pipeTo replyTo
    }
  }

  private def dialogExists(peer: Peer): Boolean = state.active.contains(peer) || state.archived.exists(_.peer == peer)

  private def isArchived(peer: Peer): Boolean = state.archived.exists(_.peer == peer)

  private def isFavourited(peer: Peer): Boolean = state.active.favourites.contains(peer)

  private def isDialogCreated(peer: Peer): Boolean = state.mobilePeers.contains(peer)

  private def isDialogShown(peer: Peer): Boolean = state.active.contains(peer)

  private def isDialogOnTop(peer: Peer): Boolean = state.mobile.headOption.exists(_.peer == peer)

  protected def fetchDialogGroups(): Future[Seq[DialogGroup]] = {
    for {
      favInfos ← Future.sequence(state.active.favourites.toSeq map (peer ⇒ getInfo(peer) map (_.getInfo))) flatMap sortActiveGroup
      groupInfos ← Future.sequence(state.active.groups.toSeq map (peer ⇒ getInfo(peer) map (_.getInfo))) flatMap sortActiveGroup
      dmInfos ← Future.sequence(state.active.dms.toSeq map (peer ⇒ getInfo(peer) map (_.getInfo))) flatMap sortActiveGroup
    } yield {
      val base = List(
        DialogGroup(DialogGroupType.Groups, groupInfos),
        DialogGroup(DialogGroupType.DirectMessages, dmInfos)
      )

      if (favInfos.nonEmpty) DialogGroup(DialogGroupType.Favourites, favInfos) :: base
      else base
    }
  }

  private def sendChatGroupsChanged(authId: Long): Future[SeqState] = {
    for {
      groups ← DialogExtension(system).fetchApiGroupedDialogs(userId)
      update = UpdateChatGroupsChanged(groups)
      seqState ← seqUpdExt.deliverClientUpdate(
        userId,
        authId,
        update,
        reduceKey = Some(s"dialogschanged_${userId}")
      )
    } yield seqState
  }

  protected def getInfo(peer: Peer): Future[DialogQueries.GetInfoResponse] =
    (context.parent ? DialogQueries.GetInfo(Some(peer))).mapTo[GetInfoResponse]

  private def sortActiveGroup(infos: Seq[DialogInfo]): Future[Seq[DialogInfo]] = {
    for {
      infosNames ← Future.sequence(infos map (info ⇒ getName(info.getPeer) map (info → _)))
    } yield infosNames.sortWith {
      case ((di1, name1), (di2, name2)) ⇒
        if (di1.getPeer.typ.isGroup && di2.getPeer.typ.isPrivate) true
        else if (di1.getPeer.typ.isPrivate && di2.getPeer.typ.isGroup) false
        else name1 < name2
    }.map(_._1)
  }

  private def getName(peer: Peer): Future[String] = {
    peer.typ match {
      case PeerType.Private ⇒
        for {
          localNameOpt ← userExt.getLocalName(userId, peer.id)
          name ← localNameOpt map FastFuture.successful getOrElse userExt.getName(peer.id, userId)
        } yield name
      case PeerType.Group ⇒ groupExt.getTitle(peer.id)
      case unknown        ⇒ FastFuture.failed(PeerErrors.UnknownPeerType(unknown))
    }
  }
}
