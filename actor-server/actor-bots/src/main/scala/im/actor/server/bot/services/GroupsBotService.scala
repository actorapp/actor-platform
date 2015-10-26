package im.actor.server.bot.services

import akka.actor.ActorSystem
import im.actor.server.bot.{ ApiToBotConversions, BotServiceBase }
import im.actor.server.group.GroupExtension
import im.actor.util.misc.IdUtils

import scala.concurrent.forkjoin.ThreadLocalRandom

private[bot] final class GroupsBotService(system: ActorSystem) extends BotServiceBase(system) with ApiToBotConversions {

  import im.actor.bots.BotMessages._
  import system.dispatcher

  private val groupExt = GroupExtension(system)

  override val handlers: Handlers = {
    case CreateGroup(title) ⇒ createGroup(title).toWeak
  }

  private def createGroup(title: String) = RequestHandler[CreateGroup, CreateGroup#Response](
    (botUserId: Int, botAuthId: Long) ⇒ {
      val groupId = IdUtils.nextIntId()
      val randomId = ThreadLocalRandom.current().nextLong()

      for {
        ack ← groupExt.create(
          groupId = groupId,
          title = title,
          clientUserId = botUserId,
          clientAuthId = botAuthId,
          randomId = randomId,
          userIds = Set.empty
        )
      } yield Right(ResponseCreateGroup(GroupOutPeer(groupId, ack.accessHash)))
    }
  )
}