package im.actor.server.bot.services

import akka.actor.ActorSystem
import im.actor.server.bot.{ ApiToBotConversions, BotServiceBase }
import im.actor.server.group.{ GroupExtension, GroupType }
import im.actor.util.misc.IdUtils

import scala.concurrent.forkjoin.ThreadLocalRandom

private[bot] final class GroupsBotService(system: ActorSystem) extends BotServiceBase(system) with ApiToBotConversions {

  import im.actor.bots.BotMessages._
  import system.dispatcher

  private val groupExt = GroupExtension(system)

  override val handlers: Handlers = {
    case CreateGroup(title)              ⇒ createGroup(title).toWeak
    case InviteUser(groupPeer, userPeer) ⇒ inviteUser(groupPeer, userPeer).toWeak
  }

  private def createGroup(title: String) = RequestHandler[CreateGroup, CreateGroup#Response](
    (botUserId: Int, botAuthId: Long, botAuthSid: Int) ⇒ {
      val groupId = IdUtils.nextIntId()
      val randomId = ThreadLocalRandom.current().nextLong()

      for {
        ack ← groupExt.create(
          groupId = groupId,
          clientUserId = botUserId,
          clientAuthSid = 0,
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
        ack ← groupExt.inviteToGroup(botUserId, groupPeer.id, userPeer.id, randomId)
      } yield Right(Void)
    }
  )
}