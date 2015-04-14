package im.actor.server.api.rpc.service

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Random

import im.actor.api.rpc.ClientData
import im.actor.api.rpc.groups.{ ResponseCreateGroup, UpdateGroupInvite }
import im.actor.api.rpc.peers.UserOutPeer
import im.actor.server.api.rpc.service.groups.GroupsServiceImpl
import im.actor.server.api.util.ACL
import im.actor.server.persist
import im.actor.server.push.SeqUpdatesManager

class GroupsServiceSpec extends BaseServiceSuite {
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

  private def createGroup(title: String, userIds: Set[Int])(implicit clientData: ClientData): ResponseCreateGroup = {
    val users = Await.result(db.run(persist.User.findByIds(userIds)), 5.seconds)
    val userPeers = users.map(user => UserOutPeer(user.id, ACL.userAccessHash(clientData.authId, user)))
    val result = Await.result(service.handleCreateGroup(Random.nextLong(), title, userPeers.toVector), 5.seconds)
    result.toOption.get
  }
}
