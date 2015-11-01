package im.actor.server.bot

import java.net.URLEncoder

import akka.actor.{ ActorSystem, ExtendedActorSystem, Extension, ExtensionId }
import akka.util.Timeout
import com.google.protobuf.ByteString
import im.actor.api.rpc.users.ApiSex
import im.actor.config.ActorConfig
import im.actor.server.acl.ACLUtils
import im.actor.server.db.DbExtension
import im.actor.server.office.EntityNotFound
import im.actor.server.persist
import im.actor.server.user.UserExtension
import org.apache.commons.codec.digest.Md5Crypt
import shardakka.keyvalue.SimpleKeyValue
import shardakka.{ Codec, IntCodec, ShardakkaExtension }

import scala.concurrent.Future
import scala.concurrent.forkjoin.ThreadLocalRandom

object BotExtension extends ExtensionId[BotExtension] {
  private[bot] val tokensKV = "BotsTokens"
  private[bot] val whTokensKV = "BotsWHTokens"

  private[bot] def whUserTokensKV(userId: Int) = s"BotsWHUserTokens-$userId"

  override def createExtension(system: ExtendedActorSystem): BotExtension = new BotExtension(system)
}

private[bot] final class BotExtension(_system: ActorSystem) extends Extension {

  import _system._

  type Token = String
  type UserId = Int
  type AuthId = Long

  private implicit val system = _system
  private implicit val timeout = Timeout(ActorConfig.defaultTimeout)

  private val db = DbExtension(system).db
  private lazy val userExt = UserExtension(system)
  private val shardakka = ShardakkaExtension(system)

  lazy val tokensKV = shardakka.simpleKeyValue(BotExtension.tokensKV, IntCodec)

  private val globalHooksKV: SimpleKeyValue[BotWebHook] = shardakka.simpleKeyValue(BotExtension.whTokensKV, BotWebHookCodec)

  private def hooksKV(userId: UserId): SimpleKeyValue[String] =
    shardakka.simpleKeyValue(BotExtension.whUserTokensKV(userId))

  /**
   * Creates a bot user
   * @param nickname
   * @param name
   * @param isAdmin
   * @return
   */
  def create(nickname: String, name: String, isAdmin: Boolean): Future[(Token, UserId)] =
    for {
      id ← userExt.nextId()
      result ← create(id, nickname, name, isAdmin)
    } yield result

  /**
   * Creates a bot user
   * @param userId
   * @param nickname
   * @param name
   * @param isAdmin
   * @return token future
   */

  def create(userId: UserId, nickname: String, name: String, isAdmin: Boolean): Future[(Token, UserId)] = {
    val token = ACLUtils.randomHash()

    for {
      user ← userExt.create(
        userId = userId,
        accessSalt = ACLUtils.nextAccessSalt(),
        Some(nickname),
        name = name,
        countryCode = "US",
        sex = ApiSex.Unknown,
        isBot = true,
        isAdmin = isAdmin
      )
      _ ← tokensKV.upsert(token, userId)
    } yield (token, userId)
  }

  /**
   * Check if the bot user already exists
   * @param userId
   * @return Future containing check result
   */
  def exists(userId: UserId): Future[Boolean] = {
    userExt.getApiStruct(userId, 0, 0) map (_ ⇒ true) recover {
      case EntityNotFound ⇒ false
    }
  }

  /**
   * Gets userId associated with token
   * @param token
   * @return user id
   */
  def findUserId(token: String): Future[Option[UserId]] =
    tokensKV.get(token)

  /**
   * Gets webhook by token
   *
   * @param token
   * @return
   */
  def findWebHook(token: String): Future[Option[BotWebHook]] = globalHooksKV.get(token)

  /**
   * Finds bot webhook
   * @param userId
   * @return
   */
  def findWebHooks(userId: UserId): Future[Seq[String]] =
    hooksKV(userId).getKeys()

  /**
   * Check if webhook exists
   * @param userId
   * @param name
   * @return
   */
  def webHookExists(userId: UserId, name: String): Future[Boolean] =
    hooksKV(userId).get(name).map(_.nonEmpty)

  /**
   * Register webhook
   * @param userId
   * @param name
   * @return
   */
  def registerWebHook(userId: UserId, name: String): Future[String] = {
    val token = genToken()
    globalHooksKV.get(token) flatMap {
      case Some(_) ⇒ registerWebHook(userId, name)
      case None ⇒
        val hook = BotWebHook(userId, name)
        for {
          _ ← globalHooksKV.upsert(token, hook)
          _ ← hooksKV(userId).upsert(name, token)
        } yield token
    }
  }

  def getHookUrl(token: String): String =
    s"${ActorConfig.baseUrl}/v1/bots/hooks/${URLEncoder.encode(token, "UTF-8")}"

  /**
   * Gets or creates bot auth id
   * @param token
   * @return auth id
   */
  def findAuthId(token: String): Future[Option[AuthId]] = {
    findUserId(token) flatMap {
      case Some(userId) ⇒ getOrCreateAuthId(userId) map (Some(_))
      case None         ⇒ Future.successful(None)
    }
  }

  /**
   * Gets or creates a bot auth id
   * @param userId
   * @return auth id
   */
  def findAuthId(userId: UserId): Future[AuthId] = getOrCreateAuthId(userId)

  private def genToken(): String = Md5Crypt.md5Crypt(ThreadLocalRandom.current().nextLong().toString.getBytes())

  private def getOrCreateAuthId(userId: Int): Future[AuthId] = {
    db.run(persist.AuthIdRepo.findFirstIdByUserId(userId)) flatMap {
      case Some(authId) ⇒
        Future.successful(authId)
      case None ⇒
        val authId = ACLUtils.randomLong()

        for {
          _ ← db.run(persist.AuthIdRepo.create(authId, None, None))
          _ ← userExt.auth(userId, authId)
        } yield authId
    }
  }
}

private object BotWebHookCodec extends Codec[BotWebHook] {
  override def fromBytes(bytes: ByteString): BotWebHook = BotWebHook.parseFrom(bytes.toByteArray)

  override def toBytes(value: BotWebHook): ByteString = ByteString.copyFrom(value.toByteArray)

  override def toString(bytes: ByteString): String = fromBytes(bytes).toString
}