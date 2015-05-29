package im.actor.server.util

import im.actor.api.rpc.AuthorizedClientData
import im.actor.server.{ models, persist }
import slick.dbio.Effect.Read
import slick.dbio.{ DBIOAction, NoStream }

import scala.concurrent.ExecutionContext

object ContactsUtils {
  def addContact(
    userId:      Int,
    phoneNumber: Long,
    name:        Option[String],
    accessSalt:  String
  )(implicit
    client: AuthorizedClientData,
    ec: ExecutionContext) = {
    persist.contact.UserContact.createOrRestore(client.userId, userId, phoneNumber, name, accessSalt)
  }

  def getLocalNameOrDefault(ownerUserId: Int, contactUser: models.User)(implicit ec: ExecutionContext): DBIOAction[String, NoStream, Read] = {
    persist.contact.UserContact.findName(ownerUserId, contactUser.id).headOption map {
      case Some(Some(name)) ⇒ name
      case _                ⇒ contactUser.name
    }
  }
}
