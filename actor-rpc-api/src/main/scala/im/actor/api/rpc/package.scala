package im.actor.api

package object rpc {
  import im.actor.api.rpc._
  import scala.reflect._
  import scalaz._, std.either._

  object Errors {
    val UnsupportedRequest = RpcError(400, "REQUEST_NOT_SUPPORTED", "Operation not supported.", false, None)
    val PhoneNumberInvalid = RpcError(400, "PHONE_NUMBER_INVALID", "Invalid phone number.", false, None)
  }

  type OkResp[+A] = (A, Vector[(Long, Update)])

  object Ok {
    def apply[A](rsp: A, updates: Vector[(Long, Update)])(implicit ev: A <:< RpcResponse): RpcError \/ (A, Vector[(Long, Update)]) =
      \/-((rsp, updates))

    def unapply[T <: OkResp[RpcResponse]](value: _ \/ T)(implicit m: ClassTag[T]) =
      value match {
        case \/-(t) => Some(t)
        case -\/(_) => None
      }
  }
}
