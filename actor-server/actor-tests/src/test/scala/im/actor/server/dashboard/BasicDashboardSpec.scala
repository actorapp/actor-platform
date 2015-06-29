//package im.actor.server.dashboard
//
//import org.scalatest.concurrent.ScalaFutures
//import org.scalatest.time.{ Seconds, Span }
//import org.scalatest.{ BeforeAndAfterAll, FlatSpec, Matchers }
//import slick.driver.PostgresDriver
//
//import im.actor.server.SqlSpecHelpers
//
//trait BasicDashboardSpec
//  extends FlatSpec
//  with SqlSpecHelpers
//  with ScalaFutures
//  with Matchers
//  with BeforeAndAfterAll {
//
//  override implicit def patienceConfig: PatienceConfig =
//    new PatienceConfig(timeout = Span(5, Seconds))
//
//  lazy val (ds, database: PostgresDriver.backend.DatabaseDef) = migrateAndInitDb()
//
//  override def afterAll(): Unit = {
//    super.afterAll()
//    database.ioExecutionContext
//    database.close()
//    ds.close()
//  }
//
//}
