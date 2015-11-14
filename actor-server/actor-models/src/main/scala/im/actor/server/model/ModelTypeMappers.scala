package im.actor.server.model

import com.trueaccord.scalapb.TypeMapper
import org.joda.time.DateTime

object ModelTypeMappers {
  private def applyDateTime(millis: Long): DateTime = new DateTime(millis)

  private def unapplyDateTime(dt: DateTime): Long = dt.getMillis

  implicit val dateTimeMapper: TypeMapper[Long, DateTime] = TypeMapper(applyDateTime)(unapplyDateTime)
}