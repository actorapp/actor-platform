package im.actor.server

import akka.actor.ActorSystem
import akka.util.Timeout
import im.actor.api.rpc.ClientData
import im.actor.api.rpc.contacts.ContactsService
import im.actor.server.user.UserExtension

import scala.concurrent.Await

trait ContactsSpecHelpers {
  val contactsService: ContactsService
  val timeout: Timeout
  val system: ActorSystem

  private val userExt = UserExtension(system)

  def addContact(userId: Int)(implicit client: ClientData): Unit = {
    val accessHash = Await.result(userExt.getAccessHash(userId, client.authId), timeout.duration)
    Await.result(contactsService.handleAddContact(userId, accessHash), timeout.duration)
  }
}