package im.actor.server.bot.services

import akka.actor.ActorSystem
import akka.util.Timeout
import im.actor.bots.BotMessages
import im.actor.concurrent.FutureResultCats
import im.actor.config.ActorConfig
import im.actor.server.bot.BotServiceBase
import org.apache.commons.codec.digest.Sha2Crypt
import shardakka.{ IntCodec, ShardakkaExtension }
import shardakka.keyvalue.SimpleKeyValue

import scala.concurrent.Future
import scala.concurrent.forkjoin.ThreadLocalRandom

final class WebHooksBotService(system: ActorSystem) extends BotServiceBase(system) with FutureResultCats[BotMessages.BotError] {
  import BotMessages._

  import system.dispatcher

  private implicit val timeout = Timeout(ActorConfig.defaultTimeout)
  private val shardakka = ShardakkaExtension(system)

  override val handlers: Handlers = {
    case RegisterHook(name: String) ⇒ registerHook(name).toWeak
    case GetHooks                   ⇒ getHooks().toWeak
  }

  private def registerHook(name: String) = RequestHandler[RegisterHook, RegisterHook#Response] {
    (botUserId: BotUserId, botAuthId: BotAuthId) ⇒
      {
        (for {
          _ ← fromFutureBoolean(BotError(406, "HOOK_EXISTS"))(exists(botUserId, name).map(!_))
          token ← fromFuture(register(botUserId, name))
        } yield Container(token)).toEither
      }
  }

  private def getHooks() = RequestHandler[GetHooks, GetHooks#Response] {
    (botUserId: BotUserId, botAuthId: BotAuthId) ⇒
      {
        for {
          hooks ← get(botUserId)
        } yield Right(ContainerList(hooks))
      }
  }

  private def hooksKV(userId: BotUserId): SimpleKeyValue[String] = shardakka.simpleKeyValue(s"bot-webhooks-$userId")

  private val globalHooksKV: SimpleKeyValue[Int] = shardakka.simpleKeyValue(s"bots-webhooks", IntCodec)

  private def exists(userId: BotUserId, name: String): Future[Boolean] = hooksKV(userId).get(name).map(_.nonEmpty)

  private def register(userId: BotUserId, name: String): Future[String] = {
    val token = genToken()
    globalHooksKV.get(token) flatMap {
      case Some(_) ⇒ register(userId, name)
      case None ⇒
        for {
          _ ← globalHooksKV.upsert(token, userId)
          _ ← hooksKV(userId).upsert(name, token)
        } yield token
    }
  }

  private def get(userId: BotUserId): Future[Seq[String]] = hooksKV((userId)).getKeys()

  private def genToken(): String = Sha2Crypt.sha256Crypt(ThreadLocalRandom.current().nextLong().toString.getBytes())
}