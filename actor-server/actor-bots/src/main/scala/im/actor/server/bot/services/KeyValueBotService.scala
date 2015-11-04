package im.actor.server.bot.services

import akka.actor.ActorSystem
import akka.util.Timeout
import im.actor.bots.BotMessages
import im.actor.config.ActorConfig
import im.actor.server.bot.BotServiceBase
import shardakka.ShardakkaExtension
import shardakka.keyvalue.SimpleKeyValue

import scala.collection.concurrent.TrieMap

private[bot] final class KeyValueBotService(system: ActorSystem) extends BotServiceBase(system) {

  import BotMessages._
  import system.dispatcher

  type Keyspace = String
  val MaxKeyspaceNameLength = 32

  private implicit val timeout = Timeout(ActorConfig.defaultTimeout)

  private val shardakka = ShardakkaExtension(system)

  private val spaces = TrieMap.empty[(BotUserId, String), SimpleKeyValue[String]]

  private def setValue(keyspace: Keyspace, key: String, value: String) = RequestHandler[SetValue, SetValue#Response](
    (botUserId: BotUserId, botAuthId: BotAuthId, botAuthSid: Int) ⇒ {
      getSpace(botUserId, keyspace) upsert (key, value) map (_ ⇒ Right(Void))
    }
  )

  private def getValue(keyspace: String, key: String) = RequestHandler[GetValue, GetValue#Response](
    (botUserId: BotUserId, botAuthId: BotAuthId, botAuthSid: Int) ⇒ {
      getSpace(botUserId, keyspace) get key map (v ⇒ Right(Container.apply(v)))
    }
  )

  private def deleteValue(keyspace: String, key: String) = RequestHandler[DeleteValue, DeleteValue#Response](
    (botUserId: BotUserId, botAuthId: BotAuthId, botAuthSid: Int) ⇒ {
      getSpace(botUserId, keyspace) delete key map (_ ⇒ Right(Void))
    }
  )

  private def getKeys(keyspace: String) = RequestHandler[GetKeys, GetKeys#Response](
    (botUserId: BotUserId, botAuthId: BotAuthId, botAuthSid: Int) ⇒ {
      getSpace(botUserId, keyspace) getKeys () map (v ⇒ Right(ContainerList.apply(v)))
    }
  )

  override def handlers: PartialFunction[RequestBody, WeakRequestHandler] = {
    case SetValue(keyspace, key, value) ⇒ setValue(keyspace, key, value).toWeak
    case GetValue(keyspace, key)        ⇒ getValue(keyspace, key).toWeak
    case DeleteValue(keyspace, key)     ⇒ deleteValue(keyspace, key).toWeak
    case GetKeys(keyspace)              ⇒ getKeys(keyspace).toWeak
  }

  private def getSpace(botUserId: BotUserId, keyspace: Keyspace): SimpleKeyValue[String] = {
    require(keyspace.length <= MaxKeyspaceNameLength, s"Maximum keyspace name length is $MaxKeyspaceNameLength")
    spaces.getOrElseUpdate(botUserId → keyspace, shardakka.simpleKeyValue(s"bot-kv-$botUserId-$keyspace"))
  }
}