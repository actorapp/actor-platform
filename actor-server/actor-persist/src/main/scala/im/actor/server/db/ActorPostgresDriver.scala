package im.actor.server.db

import com.github.tminglei.slickpg._
import com.google.protobuf.ByteString
import com.google.protobuf.wrappers.{ Int32Value, StringValue }

trait ByteStringImplicits {

  import slick.driver.PostgresDriver.api._

  implicit val byteStringColumnType = MappedColumnType.base[ByteString, Array[Byte]](
    { bs ⇒ bs.toByteArray },
    { ba ⇒ ByteString.copyFrom(ba) }
  )
}

trait ProtoWrappersImplicits {
  import slick.driver.PostgresDriver.api._

  implicit val stringValueColumnType = MappedColumnType.base[StringValue, String](
    { sv ⇒ sv.value },
    { s ⇒ StringValue(s) }
  )

  implicit val int32ValueColumnType = MappedColumnType.base[Int32Value, Int](
    { iv ⇒ iv.value },
    { i ⇒ Int32Value(i) }
  )
}

trait ActorPostgresDriver extends ExPostgresDriver
  with PgDateSupport
  with PgDate2Support
  with PgArraySupport
  with PgLTreeSupport {

  override val api =
    new API with ArrayImplicits with LTreeImplicits with DateTimeImplicits with ByteStringImplicits with ProtoWrappersImplicits
}

object ActorPostgresDriver extends ActorPostgresDriver