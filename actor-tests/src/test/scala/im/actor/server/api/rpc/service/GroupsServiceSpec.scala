package im.actor.server.api.rpc.service

import im.actor.api.rpc.ClientData
import im.actor.api.rpc.groups.UpdateGroupInvite
import im.actor.server.api.rpc.service.groups.GroupsServiceImpl
import im.actor.server.persist
import im.actor.server.push.SeqUpdatesManager

class GroupsServiceSpec extends BaseServiceSuite with GroupsServiceHelpers {
  behavior of "GroupsService"

  it should "send invites on group creation" in e1

  val seqUpdManagerRegion = SeqUpdatesManager.startRegion()
  val rpcApiService = buildRpcApiService()
  val sessionRegion = buildSessionRegion(rpcApiService, seqUpdManagerRegion)

  implicit val service = new GroupsServiceImpl(seqUpdManagerRegion)
  implicit val authService = buildAuthService(sessionRegion)
  implicit val ec = system.dispatcher

  def e1() = {
    val (user1, authId1, _) = createUser()
    val (user2, authId2, _) = createUser()

    val sessionId = createSessionId()

    implicit val clientData = ClientData(authId1, sessionId, Some(user1.id))

    createGroup("Fun group", Set(user2.id))

    whenReady(db.run(persist.sequence.SeqUpdate.find(authId2).head)) { s =>
      s.header should ===(UpdateGroupInvite.header)
    }
  }
}
