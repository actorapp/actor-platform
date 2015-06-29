package im.actor.server.db

import com.github.tminglei.slickpg._

trait ActorPostgresDriver extends ExPostgresDriver
  with PgDate2Support
  with PgArraySupport
  with PgLTreeSupport {

  override val api = new API with ArrayImplicits with LTreeImplicits with DateTimeImplicits
}

object ActorPostgresDriver extends ActorPostgresDriver