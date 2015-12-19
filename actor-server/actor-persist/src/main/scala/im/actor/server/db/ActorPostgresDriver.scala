package im.actor.server.db

import com.github.tminglei.slickpg._
import com.google.protobuf.ByteString

trait ByteStringImplicits {

  import slick.driver.PostgresDriver.api._

  implicit val byteStringColumnType = MappedColumnType.base[ByteString, Array[Byte]](
    { bs ⇒ bs.toByteArray },
    { ba ⇒ ByteString.copyFrom(ba) }
  )
}

trait ActorPostgresDriver extends ExPostgresDriver
  with PgDateSupport
  with PgDate2Support
  with PgArraySupport
  with PgLTreeSupport {

  override val api =
    new API with ArrayImplicits with LTreeImplicits with DateTimeImplicits with ByteStringImplicits
}

object ActorPostgresDriver extends ActorPostgresDriver