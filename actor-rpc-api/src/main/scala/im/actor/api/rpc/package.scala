package im.actor.api

package object rpc {
  import im.actor.api.rpc._
  import scala.reflect._
  import scalaz._, std.either._

  object Errors {
    val Internal = RpcError(500, "INTERNAL_SERVER_ERROR", "", false, None)

    val UnsupportedRequest = RpcError(400, "REQUEST_NOT_SUPPORTED", "Operation not supported.", false, None)
    val PhoneNumberInvalid = RpcError(400, "PHONE_NUMBER_INVALID", "Invalid phone number.", false, None)
    val PhoneNumberUnoccupied = RpcError(400, "PHONE_NUMBER_UNOCCUPIED", "", false, None)
    val PhoneCodeEmpty = RpcError(400, "PHONE_CODE_EMPTY", "", false, None)
    val PhoneCodeExpired = RpcError(400, "PHONE_CODE_EXPIRED", "", false, None)
    val PhoneCodeInvalid = RpcError(400, "PHONE_CODE_INVALID", "", false, None)
    val InvalidKey = RpcError(400, "INVALID_KEY", "", false, None)
  }

  type OkResp[+A] = A

  object Error {
    def apply(e: RpcError) = -\/(e)
    def unapply(v: RpcError \/ _) =
      v match {
        case \/-(_) => None
        case -\/(e) => Some(e)
      }
  }

  object Ok {
    def apply[A](rsp: A)(implicit ev: A <:< RpcResponse): RpcError \/ A =
      \/-(rsp)

    def unapply[T <: OkResp[RpcResponse]](v: _ \/ T)(implicit m: ClassTag[T]) =
      v match {
        case \/-(t) => Some(t)
        case -\/(_) => None
      }
  }
}
