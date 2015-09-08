package im.actor.server

import akka.actor.ActorSystem
import akka.util.Timeout
import im.actor.api.rpc.ClientData
import im.actor.api.rpc.groups.{ GroupsService, ResponseCreateGroup }
import im.actor.api.rpc.peers.ApiUserOutPeer
import im.actor.server.acl.ACLUtils
import im.actor.server.group.{ GroupOffice, GroupProcessorRegion, GroupViewRegion }
import slick.driver.PostgresDriver.api._

import scala.concurrent.duration._
import scala.concurrent.{ Await, ExecutionContext }
import scala.util.Random

trait GroupsServiceHelpers {
  protected def createGroup(title: String, userIds: Set[Int])(
    implicit
    clientData:  ClientData,
    db:          Database,
    service:     GroupsService,
    actorSystem: ActorSystem
  ): ResponseCreateGroup = {
    val users = Await.result(db.run(persist.User.findByIds(userIds)), 5.seconds)
    val userPeers = users.map(user ⇒ ApiUserOutPeer(user.id, ACLUtils.userAccessHash(clientData.authId, user)))
    val result = Await.result(service.handleCreateGroup(Random.nextLong(), title, userPeers.toVector), 5.seconds)
    result.toOption.get
  }

  protected def createPubGroup(title: String, description: String, userIds: Set[Int])(
    implicit
    clientData:  ClientData,
    db:          Database,
    service:     GroupsService,
    actorSystem: ActorSystem,
    region:      GroupProcessorRegion,
    timeout:     Timeout,
    ec:          ExecutionContext
  ): ResponseCreateGroup = {
    val resp = createGroup(title, userIds)
    Await.result(GroupOffice.makePublic(resp.groupPeer.groupId, description), 5.seconds)
    resp
  }

  protected def extractToken(groupId: Int)(implicit viewRegion: GroupViewRegion, ec: ExecutionContext): String = {
    implicit val timeout: Timeout = Timeout(5.seconds)
    Await.result(GroupOffice.getIntegrationToken(groupId), 5.seconds).get
  }

}
