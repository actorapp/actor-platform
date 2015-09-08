package im.actor.server.user

import im.actor.api.rpc.users._
import im.actor.server.models
import im.actor.server.models.UserPhone

import scala.language.postfixOps

object UserUtils {
  def defaultUserContactRecords(phones: Vector[Long], emails: Vector[String]): Vector[ApiContactRecord] = {
    val phoneRecords = phones map { phone ⇒
      ApiContactRecord(ApiContactType.Phone, stringValue = None, longValue = Some(phone), title = Some("Mobile phone"), subtitle = None)
    }

    val emailRecords = emails map { email ⇒
      ApiContactRecord(ApiContactType.Email, stringValue = Some(email), longValue = None, title = Some("Email"), subtitle = None)
    }

    phoneRecords ++ emailRecords
  }

  def userContactRecords(phones: Vector[models.UserPhone], emails: Vector[models.UserEmail]): Vector[ApiContactRecord] = {
    val phoneRecords = phones map { phone ⇒
      ApiContactRecord(ApiContactType.Phone, stringValue = None, longValue = Some(phone.number), title = Some(phone.title), subtitle = None)
    }

    val emailRecords = emails map { email ⇒
      ApiContactRecord(ApiContactType.Email, stringValue = Some(email.email), longValue = None, title = Some(email.title), subtitle = None)
    }

    phoneRecords ++ emailRecords
  }

  def userPhone(u: models.User, phones: Seq[UserPhone]): Option[Long] = {
    phones.headOption match {
      case Some(phone) ⇒ Some(phone.number)
      case None        ⇒ Some(0L)
    }
  }

  def normalizeLocalName(name: Option[String]) = name match {
    case n @ Some(name) if name.nonEmpty ⇒ n
    case _                               ⇒ None
  }
}
