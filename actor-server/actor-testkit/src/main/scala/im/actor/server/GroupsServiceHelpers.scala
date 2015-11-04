package im.actor.server

import akka.actor.ActorSystem
import im.actor.api.rpc.ClientData
import im.actor.api.rpc.collections.ApiMapValue
import im.actor.api.rpc.groups.{ ResponseCreateGroupObsolete, GroupsService }
import im.actor.api.rpc.peers.ApiUserOutPeer
import im.actor.server.acl.ACLUtils
import im.actor.server.group.GroupExtension
import slick.driver.PostgresDriver.api._

import scala.concurrent.duration._
import scala.concurrent.Await
import scala.util.Random

trait GroupsServiceHelpers {
  private def defaultOperationTimeout = 10.seconds

  protected def createGroup(title: String, userIds: Set[Int])(
    implicit
    clientData:  ClientData,
    db:          Database,
    service:     GroupsService,
    actorSystem: ActorSystem
  ): ResponseCreateGroupObsolete = {
    val users = Await.result(db.run(persist.UserRepo.findByIds(userIds)), defaultOperationTimeout)
    val userPeers = users.map(user ⇒ ApiUserOutPeer(user.id, ACLUtils.userAccessHash(clientData.authId, user)))
    val result = Await.result(service.handleCreateGroupObsolete(Random.nextLong(), title, userPeers.toVector), defaultOperationTimeout)
    result.toOption.get
  }

  protected def createPubGroup(title: String, description: String, userIds: Set[Int])(
    implicit
    clientData: ClientData,
    db:         Database,
    service:    GroupsService,
    system:     ActorSystem
  ): ResponseCreateGroupObsolete = {
    val resp = createGroup(title, userIds)
    Await.result(GroupExtension(system).makePublic(resp.groupPeer.groupId, description), 10.seconds)
    resp
  }

  protected def extractToken(groupId: Int)(implicit system: ActorSystem): String = {
    Await.result(GroupExtension(system).getIntegrationToken(groupId), defaultOperationTimeout).get
  }

}
