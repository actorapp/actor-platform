package im.actor.server.api.rpc.service

import im.actor.server.persist
import scala.concurrent._, duration._
import slick.driver.PostgresDriver.api._

trait ServiceSpecHelpers {
  def buildPhone(): Long = {
    75550000000L + scala.util.Random.nextInt(999999)
  }

  def createAuthId(db: Database): Long = {
    val authId = scala.util.Random.nextLong

    Await.result(db.run(persist.AuthId.create(authId, None)), 1.second)
    authId
  }
}
