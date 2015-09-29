package im.actor.server.bot

import akka.actor.{ ExtendedActorSystem, ExtensionId, ActorSystem, Extension }
import akka.util.Timeout
import im.actor.api.rpc.users.ApiSex
import im.actor.config.ActorConfig
import im.actor.server.acl.ACLUtils
import im.actor.server.db.DbExtension
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
   * @return token future
   */
  def create(name: String): Future[Token]

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
}

private[bot] final class BotExtensionImpl(_system: ActorSystem) extends BotExtension {
  import _system._

  private implicit val system = _system
  private implicit val timeout = Timeout(ActorConfig.defaultTimeout)

  private lazy val userExt = UserExtension(system)
  private lazy val tokensKV = ShardakkaExtension(system).simpleKeyValue(BotExtension.tokensKV)
  private lazy val db = DbExtension(system).db

  override def create(name: String): Future[Token] = {
    val userId = IdUtils.nextIntId()
    val token = ACLUtils.randomHash()

    for {
      user ← userExt.create(
        userId = userId,
        accessSalt = ACLUtils.nextAccessSalt(),
        name = name,
        countryCode = "US",
        sex = ApiSex.Unknown,
        isBot = true
      )
      _ ← tokensKV.upsert(token, s"$userId")
    } yield token
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

  private def getOrCreateAuthId(userId: Int): Future[AuthId] = {
    db.run(persist.AuthId.findFirstIdByUserId(userId)) flatMap {
      case Some(authId) ⇒ Future.successful(authId)
      case None ⇒
        val authId = ACLUtils.randomLong()
        db.run(persist.AuthId.create(authId, Some(userId), None)) map (_ ⇒ authId)
    }
  }
}