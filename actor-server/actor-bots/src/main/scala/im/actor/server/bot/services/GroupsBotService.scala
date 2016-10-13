package im.actor.server.bot.services

import akka.actor.ActorSystem
import im.actor.server.bot.{ ApiToBotConversions, BotServiceBase }
import im.actor.server.group.GroupErrors.InvalidExtension
import im.actor.server.group.GroupExt.Value.{ BoolValue, StringValue }
import im.actor.server.group.{ GroupExt, GroupExtension }
import im.actor.util.misc.IdUtils

import scala.concurrent.Future
import scala.concurrent.forkjoin.ThreadLocalRandom

private[bot] final class GroupsBotService(system: ActorSystem) extends BotServiceBase(system) with ApiToBotConversions {

  import im.actor.bots.BotMessages._
  import system.dispatcher

  private val groupExt = GroupExtension(system)

  override val handlers: Handlers = {
    case CreateGroup(title)                     ⇒ createGroup(title).toWeak
    case AddGroupExtString(groupId, key, value) ⇒ addGroupExtString(groupId, key, value).toWeak
    case AddGroupExtBool(groupId, key, value)   ⇒ addGroupExtBool(groupId, key, value).toWeak
    case RemoveGroupExt(groupId, key)           ⇒ removeExt(groupId, key).toWeak
    case InviteUser(groupPeer, userPeer)        ⇒ inviteUser(groupPeer, userPeer).toWeak
  }

  private def createGroup(title: String) = RequestHandler[CreateGroup, CreateGroup#Response](
    (botUserId: Int, botAuthId: Long, botAuthSid: Int) ⇒ {
      val groupId = IdUtils.nextIntId()
      val randomId = ThreadLocalRandom.current().nextLong()

      for {
        ack ← groupExt.create(
          groupId = groupId,
          clientUserId = botUserId,
          clientAuthId = 0L,
          title = title,
          randomId = randomId,
          userIds = Set.empty
        )
      } yield Right(ResponseCreateGroup(GroupOutPeer(groupId, ack.accessHash)))
    }
  )

  private def inviteUser(groupPeer: GroupOutPeer, userPeer: UserOutPeer) = RequestHandler[InviteUser, InviteUser#Response](
    (botUserId: Int, botAuthId: Long, botAuthSid: Int) ⇒ {
      // FIXME: check access hash

      val randomId = ThreadLocalRandom.current().nextLong()

      for {
        ack ← groupExt.inviteToGroup(botUserId, 0L, groupPeer.id, userPeer.id, randomId)
      } yield Right(Void)
    }
  )

  private def addGroupExtString(groupId: Int, key: String, value: String) = RequestHandler[AddGroupExtString, AddGroupExtString#Response](
    (botUserId: Int, botAuthId: Long, botAuthSid: Int) ⇒ {
      ifIsAdmin(botUserId) {
        (for (_ ← addExt(groupId, GroupExt(key, StringValue(value)))) yield Right(Void)) recover {
          case InvalidExtension ⇒ Left(BotError(500, "INVALID_EXT"))
        }
      }
    }
  )

  private def addGroupExtBool(groupId: Int, key: String, value: Boolean) = RequestHandler[AddGroupExtBool, AddGroupExtBool#Response](
    (botUserId: Int, botAuthId: Long, botAuthSid: Int) ⇒ {
      ifIsAdmin(botUserId) {
        (for (_ ← addExt(groupId, GroupExt(key, BoolValue(value)))) yield Right(Void)) recover {
          case InvalidExtension ⇒ Left(BotError(500, "INVALID_EXT"))
        }
      }
    }
  )

  private def removeExt(groupId: Int, key: String) = RequestHandler[RemoveGroupExt, RemoveGroupExt#Response](
    (botUserId: Int, botAuthId: Long, botAuthSid: Int) ⇒ {
      ifIsAdmin(botUserId) {
        groupExt.removeExt(groupId, key) map (_ ⇒ Right(Void))
      }
    }
  )

  private def addExt(groupId: Int, ext: GroupExt): Future[Unit] = groupExt.addExt(groupId: Int, ext: GroupExt)

}
