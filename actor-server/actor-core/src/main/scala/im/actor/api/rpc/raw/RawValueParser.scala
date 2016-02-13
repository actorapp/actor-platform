package im.actor.api.rpc.raw

import im.actor.api.rpc.collections._

import scala.util.Try

trait RawValueParser[A] {
  def apply(raw: ApiRawValue): Option[A]
}

// target parsing type is ApiArrayValue
// todo: add parser for ApiMapValue, to make Map[String, Any]. Not too usefull
object RawValueParser {

  def parse[A](raw: ApiRawValue)(implicit parser: RawValueParser[A]): Option[A] = parser(raw)

  import shapeless._

  implicit val booleanParser = new RawValueParser[Boolean] {
    def apply(raw: ApiRawValue): Option[Boolean] = raw match {
      case ApiStringValue(v) ⇒ Try(v.toBoolean).toOption
      case _                 ⇒ None
    }
  }

  implicit val stringParser = new RawValueParser[String] {
    def apply(raw: ApiRawValue): Option[String] = raw match {
      case ApiStringValue(v) ⇒ Some(v)
      case _                 ⇒ None
    }
  }

  implicit val doubleParser = new RawValueParser[Double] {
    def apply(raw: ApiRawValue): Option[Double] = raw match {
      case ApiDoubleValue(v) ⇒ Some(v)
      case _                 ⇒ None
    }
  }

  implicit val intParser = new RawValueParser[Int] {
    def apply(raw: ApiRawValue): Option[Int] = raw match {
      case ApiInt32Value(v) ⇒ Some(v)
      case _                ⇒ None
    }
  }

  implicit val longParser = new RawValueParser[Long] {
    def apply(raw: ApiRawValue): Option[Long] = raw match {
      case ApiInt64Value(v) ⇒ Some(v)
      case _                ⇒ None
    }
  }

  implicit val hnilParser: RawValueParser[HNil] = new RawValueParser[HNil] {
    def apply(raw: ApiRawValue): Option[HNil] = raw match {
      case ApiArrayValue(arr) if arr.isEmpty ⇒ Some(HNil)
      case _                                 ⇒ None
    }
  }

  // we know how to parse ApiRawValue to hcons only if ApiRawValue is Array
  implicit def hconsParser[H: RawValueParser, T <: HList: RawValueParser]: RawValueParser[H :: T] = new RawValueParser[H :: T] {
    def apply(raw: ApiRawValue): Option[H :: T] = raw match {
      case ApiArrayValue(arr) ⇒ arr match {
        case h +: t ⇒
          for {
            // todo: intead of implicitly - provide apply method in object, that will get implicitly
            head ← implicitly[RawValueParser[H]].apply(h)
            tail ← implicitly[RawValueParser[T]].apply(ApiArrayValue(t))
          } yield head :: tail
      }
      case _ ⇒ None
    }
  }

  implicit def caseClassParser[A, R <: HList](implicit gen: Generic[A] { type Repr = R }, reprParser: RawValueParser[R]): RawValueParser[A] = new RawValueParser[A] {
    def apply(raw: ApiRawValue): Option[A] = reprParser.apply(raw).map(gen.from)
  }

}
