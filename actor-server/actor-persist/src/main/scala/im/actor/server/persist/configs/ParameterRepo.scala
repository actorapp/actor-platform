package im.actor.server.persist.configs

import im.actor.server.model.{ PeerType, Peer }
import im.actor.server.model.configs.Parameter
import slick.driver.PostgresDriver.api._

import scala.concurrent.ExecutionContext

final class ParameterTable(tag: Tag) extends Table[Parameter](tag, "config_parameters") {
  def userId = column[Int]("user_id", O.PrimaryKey)

  def key = column[String]("key", O.PrimaryKey)

  def value = column[Option[String]]("value")

  def * = (userId, key, value) <> ((Parameter.apply _).tupled, Parameter.unapply)
}

object ParameterRepo {
  val parameters = TableQuery[ParameterTable]

  def createOrUpdate(parameter: Parameter) =
    parameters.insertOrUpdate(parameter)

  def find(userId: Int) =
    parameters.filter(_.userId === userId).result

  private def valuesByUserIdAndKey(userId: Rep[Int], key: Rep[String]) =
    parameters.filter(p ⇒ p.userId === userId && p.key === key).map(_.value)

  private def byUserIdAndKeyLike(userId: Rep[Int], pattern: Rep[String]) =
    parameters.filter(p ⇒ p.userId === userId && p.key.like(pattern))

  private val byUserIdAndKeyLikeC = Compiled(byUserIdAndKeyLike _)

  private def firstByUserIdAndKey(userId: Rep[Int], key: Rep[String]) = valuesByUserIdAndKey(userId, key).take(1)

  private val firstByUserIdAndKeyC = Compiled(firstByUserIdAndKey _)

  def findValue(userId: Int, key: String)(implicit ec: ExecutionContext) =
    firstByUserIdAndKeyC((userId, key)).result.headOption map (_.flatten)

  def findValue(userId: Int, key: String, default: String)(implicit ec: ExecutionContext) =
    firstByUserIdAndKeyC((userId, key)).result.headOption map (_.flatten.getOrElse(default))

  def findBooleanValue(userId: Int, key: String, default: Boolean)(implicit ec: ExecutionContext) =
    findValue(userId, key) map {
      case Some("false") ⇒ false
      case Some("true")  ⇒ true
      case _             ⇒ default
    }

  def findPeerRingtone(userId: Int)(implicit ec: ExecutionContext): DBIO[Seq[(Peer, String)]] =
    extractPeerParams[(Peer, String)](userId, prefix = "app.ios.mobile.notification.chat.sound.") {
      case (key, optValue) ⇒
        key.split("_").toList match {
          case "GROUP" :: id :: Nil ⇒
            optValue map (v ⇒ (Peer(PeerType.Group, id.toInt), v))
          case "PRIVATE" :: id :: Nil ⇒
            optValue map (v ⇒ (Peer(PeerType.Private, id.toInt), v))
          case _ ⇒ None
        }
    }

  def findPeerNotifications(userId: Int, deviceType: String)(implicit ec: ExecutionContext): DBIO[Seq[(Peer, Boolean)]] = {
    def boolValue(value: Option[String]) = value match {
      case Some("false") ⇒ false
      case _             ⇒ true
    }

    extractPeerParams[(Peer, Boolean)](userId, prefix = s"category.$deviceType.notification.chat.") {
      case (key, value) ⇒
        key.split("\\.").toList match {
          case peerStr :: "enabled" :: Nil ⇒
            peerStr.split("_").toList match {
              case "GROUP" :: id :: Nil ⇒
                Some((Peer(PeerType.Group, id.toInt), boolValue(value)))
              case "PRIVATE" :: id :: Nil ⇒
                Some((Peer(PeerType.Private, id.toInt), boolValue(value)))
              case _ ⇒ None
            }
          case _ ⇒ None
        }
    }
  }

  private type KeyValue = (String, Option[String])

  // key passed in `f` comes without prefix
  private def extractPeerParams[T](userId: Int, prefix: String)(f: KeyValue ⇒ Option[T])(implicit ec: ExecutionContext): DBIO[Seq[T]] =
    byUserIdAndKeyLikeC((userId, s"$prefix%")).result map { params ⇒
      val kvs: Seq[KeyValue] = params map (p ⇒ p.key.drop(prefix.length) → p.value)
      (kvs map f).flatten
    }

}