package im.actor.api.rpc.raw

import akka.actor.ActorSystem
import cats.data.Xor
import im.actor.api.rpc.collections._
import im.actor.api.rpc.{ ClientData, RpcError }
import play.api.libs.json._

import scala.concurrent.Future

trait ProductImplicits {

  /**
   * Implicit conversion for case classes and case objects.
   * Allows to represent case class as ApiMap.
   * Case classes with 0 fields and case objects are converted to empty ApiMap
   *
   * @param product case class or case object
   */
  implicit class Product2ApiValue(product: Product) {
    def asApiArray: ApiArrayValue =
      if (product.productArity == 0) {
        //case object or empty case class
        ApiArrayValue(Vector.empty[ApiRawValue])
      } else {
        val items = product.getClass.getDeclaredFields.foldLeft(Vector.empty[ApiRawValue]) { (a, f) ⇒
          f.setAccessible(true)
          a :+ toApiRawValue(f.get(product))
        }
        ApiArrayValue(items)
      }
    def asApiMap: ApiMapValue =
      if (product.productArity == 0) {
        //case object or empty case class
        ApiMapValue(Vector.empty[ApiMapValueItem])
      } else {
        val items = product.getClass.getDeclaredFields.foldLeft(Vector.empty[ApiMapValueItem]) { (a, f) ⇒
          f.setAccessible(true)
          a :+ ApiMapValueItem(f.getName, toApiRawValue(f.get(product)))
        }
        ApiMapValue(items)
      }
  }

  private def toApiRawValue: PartialFunction[Any, ApiRawValue] = {
    case b: Boolean ⇒ ApiStringValue(b.toString)
    case s: String  ⇒ ApiStringValue(s)
    case d: Double  ⇒ ApiDoubleValue(d)
    case i: Int     ⇒ ApiInt32Value(i)
    case l: Long    ⇒ ApiInt64Value(l)
    case s: Seq[_]  ⇒ ApiArrayValue((s map toApiRawValue).toVector)
    case m: Map[String @unchecked, _] ⇒
      ApiMapValue(m.toVector map { case (k, v) ⇒ ApiMapValueItem(k, toApiRawValue(v)) })
    case p: Product ⇒ p.asApiMap
  }

}

abstract class MapStyleRawApiService(system: ActorSystem) extends RawApiService(system) with ProductImplicits {
  import im.actor.api.rpc.FutureResultRpc._
  import system.dispatcher

  type Request

  final override def handleRequests: Handler = implicit client ⇒ params ⇒ new PartialFunction[String, Future[Response]] {
    override def isDefinedAt(name: String): Boolean = validateRequest(None).isDefinedAt(name)

    override def apply(name: String): Future[Response] = (for {
      request ← fromXor(toRequest(name, params))
      result ← fromFutureXor(handleInternal(client)(request))
    } yield result).value
  }

  protected def validateRequest: Option[JsObject] ⇒ PartialFunction[String, RpcError Xor Request]

  protected def handleInternal: ClientData ⇒ PartialFunction[Request, Future[Response]]

  private def toRequest(name: String, optParams: Option[ApiRawValue]): RpcError Xor Request = {
    val jsParams = optParams map { params ⇒
      convert(params) match {
        case o: JsObject ⇒ Xor.right(Some(o))
        case _           ⇒ Xor.left(RpcError(400, "INVALID_PARAMS", "Wrong parameter format; should be MapValue", false, None))
      }
    } getOrElse Xor.right(None)
    jsParams flatMap { o ⇒ validateRequest(o)(name) }
  }

  private def convert(raw: ApiRawValue): JsValue = raw match {
    case ApiMapValue(items) ⇒
      val fields = items map { case ApiMapValueItem(key, value) ⇒ key → convert(value) }
      JsObject(fields)
    case ApiStringValue(t)     ⇒ JsString(t)
    case ApiDoubleValue(d)     ⇒ JsNumber(d)
    case ApiInt32Value(i)      ⇒ JsNumber(i)
    case ApiInt64Value(l)      ⇒ JsNumber(l)
    case ApiArrayValue(values) ⇒ JsArray(values map convert)
  }
}

