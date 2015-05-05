package im.actor.server.db

import com.github.tminglei.slickpg._
import slick.driver.PostgresDriver

trait ExtendedPostgresqlDriver extends PostgresDriver
  with PgArraySupport
  with PgLTreeSupport {

  override val api = new API with ArrayImplicits with LTreeImplicits {}
}

object ExtendedPostgresqlDriver extends ExtendedPostgresqlDriver