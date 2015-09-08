package im.actor.server.user

import akka.actor.ActorSystem
import akka.pattern.pipe

import im.actor.api.rpc.users.ApiUser
import im.actor.server.{ KeyValueMappings, ApiConversions }
import ApiConversions._
import im.actor.server.acl.ACLUtils
import ContactsUtils.localNameKey
import shardakka.ShardakkaExtension

private[user] trait UserQueriesHandlers {
  self: UserProcessor ⇒

  import UserQueries._

  protected def getAuthIds(state: User): Unit = {
    sender() ! GetAuthIdsResponse(state.authIds.toSeq)
  }

  protected def getApiStruct(state: User, clientUserId: Int, clientAuthId: Long)(implicit system: ActorSystem): Unit = {
    val kv = ShardakkaExtension(system).simpleKeyValue(KeyValueMappings.LocalNames)
    kv.get(localNameKey(clientUserId, userId)) map { localName ⇒
      GetApiStructResponse(ApiUser(
        id = userId,
        accessHash = ACLUtils.userAccessHash(clientAuthId, userId, state.accessSalt),
        name = state.name,
        localName = UserUtils.normalizeLocalName(localName),
        sex = Some(state.sex),
        avatar = state.avatar,
        phone = state.phones.headOption.orElse(Some(0)),
        isBot = Some(state.isBot),
        contactInfo = UserUtils.defaultUserContactRecords(state.phones.toVector, state.emails.toVector),
        nick = state.nickname,
        about = state.about
      ))
    } pipeTo sender()
  }

  protected def getContactRecords(state: User): Unit =
    sender() ! GetContactRecordsResponse(state.phones, state.emails)

  protected def checkAccessHash(state: User, senderAuthId: Long, accessHash: Long): Unit =
    sender() ! CheckAccessHashResponse(isCorrect = accessHash == ACLUtils.userAccessHash(senderAuthId, userId, state.accessSalt))

  protected def getAccessHash(state: User, clientAuthId: Long): Unit =
    sender() ! GetAccessHashResponse(ACLUtils.userAccessHash(clientAuthId, userId, state.accessSalt))
}
