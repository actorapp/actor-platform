package im.actor.server.user

import akka.pattern.pipe

import im.actor.api.rpc.users.{ User ⇒ ApiUser }
import im.actor.server.api.ApiConversions._
import im.actor.server.util.{ ACLUtils, UserUtils }
import im.actor.server.{ persist ⇒ p }

private[user] trait UserQueriesHandlers {
  self: UserProcessor ⇒

  import UserQueries._

  protected def handleQuery(q: UserQuery, state: User): Unit =
    q match {
      case GetAuthIds(_) ⇒ getAuthIds(state)
    }

  protected def getAuthIds(state: User): Unit = {
    sender() ! GetAuthIdsResponse(state.authIds.toSeq)
  }

  protected def getApiStruct(state: User, clientUserId: Int, clientAuthId: Long): Unit = {
    db.run(p.contact.UserContact.findName(clientUserId: Int, state.id).headOption map (_.getOrElse(None))).map { localName ⇒
      GetApiStructResponse(ApiUser(
        id = state.id,
        accessHash = ACLUtils.userAccessHash(clientAuthId, state.id, state.accessSalt),
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
    }.pipeTo(sender())
  }
}
