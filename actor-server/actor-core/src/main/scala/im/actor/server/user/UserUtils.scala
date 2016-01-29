package im.actor.server.user

import akka.actor.ActorSystem
import im.actor.api.rpc.users._
import im.actor.server.model.{ UserEmail, User, UserPhone }
import im.actor.server.user.UserErrors.UserNotFound

import scala.language.postfixOps

object UserUtils {
  def defaultUserContactRecords(phones: Vector[Long], emails: Vector[String], socialContacts: Vector[SocialContact]): Vector[ApiContactRecord] = {
    val phoneRecords = phones map { phone ⇒
      ApiContactRecord(ApiContactType.Phone, stringValue = None, longValue = Some(phone), title = Some("Mobile phone"), subtitle = None, typeSpec = None)
    }

    val emailRecords = emails map { email ⇒
      ApiContactRecord(ApiContactType.Email, stringValue = Some(email), longValue = None, title = Some("Email"), subtitle = None, typeSpec = None)
    }

    val socialRecords = socialContacts map { contact ⇒
      ApiContactRecord(ApiContactType.Social, stringValue = Some(contact.value), longValue = None, title = Some(contact.title), subtitle = None, typeSpec = None)
    }

    phoneRecords ++ emailRecords ++ socialRecords
  }

  def userContactRecords(phones: Vector[UserPhone], emails: Vector[UserEmail]): Vector[ApiContactRecord] = {
    val phoneRecords = phones map { phone ⇒
      ApiContactRecord(ApiContactType.Phone, stringValue = None, longValue = Some(phone.number), title = Some(phone.title), subtitle = None, typeSpec = None)
    }

    val emailRecords = emails map { email ⇒
      ApiContactRecord(ApiContactType.Email, stringValue = Some(email.email), longValue = None, title = Some(email.title), subtitle = None, typeSpec = None)
    }

    phoneRecords ++ emailRecords
  }

  def userPhone(u: User, phones: Seq[UserPhone]): Option[Long] = {
    phones.headOption match {
      case Some(phone) ⇒ Some(phone.number)
      case None        ⇒ Some(0L)
    }
  }

  def normalizeLocalName(name: Option[String]) = name match {
    case n @ Some(name) if name.nonEmpty ⇒ n
    case _                               ⇒ None
  }

  def safeGetUser(userId: Int, clientUserId: Int, clientAuthId: Long)(implicit system: ActorSystem) = {
    import system.dispatcher
    UserExtension(system)
      .getApiStruct(userId, clientUserId, clientAuthId)
      .map(Some(_))
      .recover {
        case _: UserNotFound ⇒ None
      }
  }
}
