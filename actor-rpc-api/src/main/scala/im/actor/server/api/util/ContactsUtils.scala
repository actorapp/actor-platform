package im.actor.server.api.util

import scala.concurrent.ExecutionContext

import slick.dbio.Effect.{ All, Write }
import slick.dbio.{ NoStream, DBIOAction }

import im.actor.api.rpc.AuthorizedClientData
import im.actor.api.rpc.contacts.UpdateContactsAdded
import im.actor.server.persist
import im.actor.server.push.SeqUpdatesManagerRegion

object ContactsUtils {

  import im.actor.server.push.SeqUpdatesManager._

  def addContactSendUpdate(
    userId:      Int,
    phoneNumber: Long,
    name:        Option[String],
    accessSalt:  String
  )(implicit
    client: AuthorizedClientData,
    ec:                  ExecutionContext,
    seqUpdManagerRegion: SeqUpdatesManagerRegion): DBIOAction[(Sequence, Array[Byte]), NoStream, Write with All] = {
    for {
      _ ← persist.contact.UserContact.createOrRestore(client.userId, userId, phoneNumber, name, accessSalt)
      seqstate ← broadcastClientUpdate(UpdateContactsAdded(Vector(userId)))
    } yield seqstate
  }
}
