package im.actor.server.dashboard

import org.scalatest.{ BeforeAndAfterAll, Matchers, FlatSpec }
import org.scalatest.concurrent.ScalaFutures
import slick.driver.PostgresDriver

import im.actor.server.SqlSpecHelpers

trait BasicDashboardSpec
  extends FlatSpec
  with SqlSpecHelpers
  with ScalaFutures
  with Matchers
  with BeforeAndAfterAll {

  val (ds, database: PostgresDriver.backend.DatabaseDef) = migrateAndInitDb()

  override def afterAll(): Unit = {
    super.afterAll()
    database.ioExecutionContext
    database.close()
    ds.close()
  }

}
