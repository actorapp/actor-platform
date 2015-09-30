package im.actor.server.bot

import akka.actor.{ ExtendedActorSystem, ExtensionId, ActorSystem, Extension }
import akka.util.Timeout
import im.actor.api.rpc.users.ApiSex
import im.actor.config.ActorConfig
import im.actor.server.acl.ACLUtils
import im.actor.server.db.DbExtension
import im.actor.server.office.EntityNotFound
import im.actor.server.user.UserExtension
import im.actor.server.persist
import im.actor.util.misc.IdUtils
import shardakka.ShardakkaExtension

import scala.concurrent.Future

object BotExtension extends ExtensionId[BotExtension] {
  private[bot] val tokensKV = "BotsTokens"

  override def createExtension(system: ExtendedActorSystem): BotExtension = new BotExtensionImpl(system)
}

trait BotExtension extends Extension {
  type Token = String
  type UserId = Int
  type AuthId = Long

  /**
   * Creates a bot user
   *
   * @param userId
   * @param nickname
   * @param name
   * @return token future
   */
  def create(userId: UserId, nickname: String, name: String): Future[Token]

  /**
   * Check if the bot user already exists
   * @param userId
   * @return Future containing check result
   */
  def exists(userId: Int): Future[Boolean]

  /**
   * Gets userId associated with token
   *
   * @param token
   * @return user id
   */
  def getUserId(token: String): Future[Option[UserId]]

  /**
   * Gets or creates bot auth id
   * @param token
   * @return auth id
   */
  def getAuthId(token: String): Future[Option[AuthId]]

  /**
   * Gets or creates a bot auth id
   * @param userId
   * @return auth id
   */
  def getAuthId(userId: UserId): Future[AuthId]
}

private[bot] final class BotExtensionImpl(_system: ActorSystem) extends BotExtension {
  import _system._

  private implicit val system = _system
  private implicit val timeout = Timeout(ActorConfig.defaultTimeout)

  private lazy val userExt = UserExtension(system)
  private lazy val tokensKV = ShardakkaExtension(system).simpleKeyValue(BotExtension.tokensKV)
  private lazy val db = DbExtension(system).db

  override def create(userId: UserId, nickname: String, name: String): Future[Token] = {
    val token = ACLUtils.randomHash()

    for {
      user ← userExt.create(
        userId = userId,
        accessSalt = ACLUtils.nextAccessSalt(),
        Some(nickname),
        name = name,
        countryCode = "US",
        sex = ApiSex.Unknown,
        isBot = true
      )
      _ ← tokensKV.upsert(token, s"$userId")
    } yield token
  }

  override def exists(userId: UserId): Future[Boolean] = {
    userExt.getApiStruct(userId, 0, 0) map (_ ⇒ true) recover {
      case EntityNotFound ⇒ false
    }
  }

  override def getUserId(token: String): Future[Option[UserId]] = {
    for {
      tokOpt ← tokensKV.get(token)
    } yield tokOpt map (_.toInt)
  }

  override def getAuthId(token: String): Future[Option[AuthId]] = {
    getUserId(token) flatMap {
      case Some(userId) ⇒ getOrCreateAuthId(userId) map (Some(_))
      case None         ⇒ Future.successful(None)
    }
  }

  override def getAuthId(userId: UserId): Future[AuthId] = getOrCreateAuthId(userId)

  private def getOrCreateAuthId(userId: Int): Future[AuthId] = {
    db.run(persist.AuthId.findFirstIdByUserId(userId)) flatMap {
      case Some(authId) ⇒
        Future.successful(authId)
      case None ⇒
        val authId = ACLUtils.randomLong()

        for {
          _ ← db.run(persist.AuthId.create(authId, None, None))
          _ ← userExt.auth(userId, authId)
        } yield authId
    }
  }
}