package im.actor.server.user

import akka.actor.ActorSystem
import akka.pattern.pipe
import im.actor.api.rpc.users.ApiUser
import im.actor.server.ApiConversions._
import im.actor.server.acl.ACLUtils

import scala.concurrent.Future

private[user] trait UserQueriesHandlers {
  self: UserProcessor ⇒

  import UserQueries._

  protected def getAuthIds(state: User): Unit =
    sender() ! GetAuthIdsResponse(state.authIds)

  protected def getApiStruct(state: User, clientUserId: Int, clientAuthId: Long)(implicit system: ActorSystem): Unit = {
    (for {
      localName ← if (clientUserId == state.id || clientUserId == 0)
        Future.successful(None)
      else
        userExt.getLocalName(clientUserId, state.id)
    } yield GetApiStructResponse(ApiUser(
      id = userId,
      accessHash = ACLUtils.userAccessHash(clientAuthId, userId, state.accessSalt, aclMD),
      name = state.name,
      localName = UserUtils.normalizeLocalName(localName),
      sex = Some(state.sex),
      avatar = state.avatar,
      phone = state.phones.headOption.orElse(Some(0)),
      isBot = Some(state.isBot),
      contactInfo = UserUtils.defaultUserContactRecords(state.phones.toVector, state.emails.toVector, state.socialContacts.toVector),
      nick = state.nickname,
      about = state.about,
      external = state.external,
      preferredLanguages = state.preferredLanguages.toVector,
      timeZone = state.timeZone
    ))) pipeTo sender()
  }

  protected def getContactRecords(state: User): Unit =
    sender() ! GetContactRecordsResponse(state.phones, state.emails)

  protected def checkAccessHash(state: User, senderAuthId: Long, accessHash: Long): Unit =
    sender() ! CheckAccessHashResponse(isCorrect = accessHash == ACLUtils.userAccessHash(senderAuthId, userId, state.accessSalt))

  protected def getAccessHash(state: User, clientAuthId: Long): Unit =
    sender() ! GetAccessHashResponse(ACLUtils.userAccessHash(clientAuthId, userId, state.accessSalt))

  protected def getUser(state: User): Unit = sender() ! state

  protected def isAdmin(state: User): Unit = sender() ! IsAdminResponse(state.isAdmin.getOrElse(false))

  protected def getName(state: User): Unit = sender() ! GetNameResponse(state.name)
}
