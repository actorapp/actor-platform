package im.actor.api.rpc.raw

import im.actor.api.rpc.collections._

import scala.util.Try

trait RawValueParser[A] {
  def parse(raw: ApiRawValue): Option[A]
}

object RawValueParser extends RawValueParserTypeclassInstances {
  def apply[T: RawValueParser]: RawValueParser[T] = implicitly[RawValueParser[T]]

  def parse[A: RawValueParser](raw: ApiRawValue): Option[A] = RawValueParser[A].parse(raw)
}

// todo: add nesting
trait RawValueParserTypeclassInstances {
  import shapeless._

  implicit val booleanParser = new RawValueParser[Boolean] {
    def parse(raw: ApiRawValue): Option[Boolean] = raw match {
      case ApiStringValue(v) ⇒ Try(v.toBoolean).toOption
      case _                 ⇒ None
    }
  }

  implicit val stringParser = new RawValueParser[String] {
    def parse(raw: ApiRawValue): Option[String] =
      Option(raw) collect { case ApiStringValue(v) ⇒ v }
  }

  implicit val doubleParser = new RawValueParser[Double] {
    def parse(raw: ApiRawValue): Option[Double] =
      Option(raw) collect { case ApiDoubleValue(v) ⇒ v }
  }

  implicit val intParser = new RawValueParser[Int] {
    def parse(raw: ApiRawValue): Option[Int] =
      Option(raw) collect { case ApiInt32Value(v) ⇒ v }
  }

  implicit val longParser = new RawValueParser[Long] {
    def parse(raw: ApiRawValue): Option[Long] =
      Option(raw) collect { case ApiInt64Value(v) ⇒ v }
  }

  // parse sequences with elements of same type
  implicit def listParser[T: RawValueParser] = new RawValueParser[Seq[T]] {
    def parse(raw: ApiRawValue): Option[Seq[T]] = raw match {
      case ApiArrayValue(values) if values.isEmpty ⇒ Some(Seq.empty[T])
      case ApiArrayValue(values) ⇒
        val parsedSeq = values flatMap RawValueParser[T].parse
        if (parsedSeq.isEmpty) None else Some(parsedSeq)
      case _ ⇒ None
    }
  }

  // parse maps with values of same type
  implicit def mapParser[V: RawValueParser] = new RawValueParser[Map[String, V]] {
    def parse(raw: ApiRawValue): Option[Map[String, V]] = raw match {
      case ApiMapValue(items) if items.isEmpty ⇒ Some(Map.empty[String, V])
      case ApiMapValue(items) ⇒
        val parsedKVs = items flatMap {
          case ApiMapValueItem(k, v) ⇒
            RawValueParser[V].parse(v) map (k → _)
        }
        if (parsedKVs.isEmpty) None else Some(parsedKVs.toMap)
      case _ ⇒ None
    }
  }

  implicit val hnilParser: RawValueParser[HNil] = new RawValueParser[HNil] {
    def parse(raw: ApiRawValue): Option[HNil] = raw match {
      case ApiArrayValue(arr) if arr.isEmpty ⇒ Some(HNil)
      case _                                 ⇒ None
    }
  }

  // we know how to parse ApiRawValue to hcons only if ApiRawValue is Array
  implicit def hconsParser[H: RawValueParser, T <: HList: RawValueParser]: RawValueParser[H :: T] = new RawValueParser[H :: T] {
    def parse(raw: ApiRawValue): Option[H :: T] = raw match {
      case ApiArrayValue(arr) ⇒ arr match {
        case h +: t ⇒
          for {
            head ← RawValueParser[H].parse(h)
            tail ← RawValueParser[T].parse(ApiArrayValue(t))
          } yield head :: tail
      }
      case _ ⇒ None
    }
  }

  implicit def caseClassParser[A, R <: HList](implicit gen: Generic[A] { type Repr = R }, reprParser: RawValueParser[R]): RawValueParser[A] = new RawValueParser[A] {
    def parse(raw: ApiRawValue): Option[A] = reprParser.parse(raw).map(gen.from)
  }

}
