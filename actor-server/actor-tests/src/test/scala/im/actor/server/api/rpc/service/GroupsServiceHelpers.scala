package im.actor.server.api.rpc.service

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Random

import akka.actor.ActorSystem
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc.ClientData
import im.actor.api.rpc.groups.{ GroupsService, ResponseCreateGroup }
import im.actor.api.rpc.peers.UserOutPeer
import im.actor.server.persist
import im.actor.server.util.ACLUtils

trait GroupsServiceHelpers {
  protected def createGroup(title: String, userIds: Set[Int])(
    implicit
    clientData:  ClientData,
    db:          Database,
    service:     GroupsService,
    actorSystem: ActorSystem
  ): ResponseCreateGroup = {
    val users = Await.result(db.run(persist.User.findByIds(userIds)), 5.seconds)
    val userPeers = users.map(user ⇒ UserOutPeer(user.id, ACLUtils.userAccessHash(clientData.authId, user)))
    val result = Await.result(service.handleCreateGroup(Random.nextLong(), title, userPeers.toVector), 5.seconds)
    result.toOption.get
  }
}
