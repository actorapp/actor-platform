package im.actor.server.persist.db

import slick.driver.PostgresDriver.api.Database
import scala.slick.jdbc.StaticQuery.interpolation
import scala.concurrent.{ ExecutionContext, Future }
import com.typesafe.config.ConfigFactory

object Db {
  val db = Database.forConfig("jdbc", ConfigFactory.load())

  def check() = {
    db.withSession { implicit s =>
      assert(sql"select 1".as[Int].first == 1)
    }
  }

  def async[T](f: => T)(implicit ec: ExecutionContext = ExecutionContext.Implicits.global): Future[T] = Future {
    db.withDynSession(f)
  }
}
