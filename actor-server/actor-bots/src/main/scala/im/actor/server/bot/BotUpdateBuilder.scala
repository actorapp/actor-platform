package im.actor.server.bot

import java.util.Base64

import akka.actor.ActorSystem
import im.actor.api.rpc.Update
import im.actor.api.rpc.messaging.UpdateMessage
import im.actor.api.rpc.sequence.UpdateRawUpdate
import im.actor.bots.BotMessages._
import im.actor.server.acl.ACLUtils
import im.actor.server.group.GroupUtils
import im.actor.server.user.UserExtension

import scala.concurrent.Future
import scala.language.postfixOps

final class BotUpdateBuilder(botUserId: Int, botAuthId: Long, system: ActorSystem) extends ApiToBotConversions {

  import system.dispatcher

  implicit val _system = system
  val userExt = UserExtension(system)

  def apply(seq: Int, upd: Update): Future[Option[BotFatSeqUpdate]] = {
    val updateOptFuture = upd match {
      case update: UpdateMessage ⇒

        if (update.senderUserId != botUserId) {
          for {
            apiOutPeer ← ACLUtils.getOutPeer(update.peer, botAuthId)
            senderAccessHash ← userExt.getAccessHash(update.senderUserId, botAuthId)
          } yield Some(Message(
            peer = apiOutPeer,
            sender = UserOutPeer(update.senderUserId, senderAccessHash),
            date = update.date,
            randomId = update.randomId,
            message = update.message
          ))
        } else
          Future.successful(None)
      case update: UpdateRawUpdate ⇒
        Future.successful(Some(RawUpdate(
          `type` = update.`type`,
          data = Base64.getEncoder.encodeToString(update.bytes)
        )))
      case _ ⇒ Future.successful(None)
    }

    updateOptFuture flatMap {
      case Some(body) ⇒
        val userIds = upd._relatedUserIds
        for {
          (apiGroups, apiUsers) ← GroupUtils.getGroupsUsers(Seq.empty[Int], userIds, botUserId, botAuthId)
        } yield Some(BotFatSeqUpdate(
          seq = seq,
          body = body,
          users = buildUsers(apiUsers),
          groups = buildGroups(apiGroups)
        ))
      case None ⇒ Future.successful(None)
    }
  }
}