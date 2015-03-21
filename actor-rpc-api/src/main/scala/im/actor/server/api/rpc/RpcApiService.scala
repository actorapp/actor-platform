package im.actor.server.api.rpc

import scala.concurrent._
import scalaz._

import akka.actor._
import akka.pattern.pipe
import scodec.bits._
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc._
import im.actor.api.rpc.codecs._
import im.actor.server.api.rpc.service.auth.AuthServiceImpl

object RpcApiService {

  @SerialVersionUID(1L)
  case class HandleRpcRequest(messageId: Long, requestBytes: BitVector, clientData: ClientData)

  @SerialVersionUID(1L)
  case class RpcResponse(messageId: Long, responseBytes: BitVector)

  def props()(implicit db: Database) = Props(classOf[RpcApiService], db)

  @SerialVersionUID(1L)
  private[rpc] case object Initialize

}

class RpcApiService(implicit db: Database) extends Actor with ActorLogging {

  import RpcApiService._

  type Chain = PartialFunction[RpcRequest, ClientData => Future[RpcError \/ RpcOk]]

  implicit val ec: ExecutionContext = context.dispatcher

  override def preStart(): Unit = {
    super.preStart()
    self ! Initialize
  }

  def receive: Receive = {
    case Initialize =>
      implicit val s = context.system

      val services: Seq[Service] = Seq(new AuthServiceImpl)
      val chain: Chain = services.map(_.handleRequestPartial).reduce { (a, b) =>
        a.orElse(b)
      }

      context.become(initialized(chain), discardOld = true)
  }

  def initialized(chain: Chain): Receive = {
    case HandleRpcRequest(messageId, requestBytes, clientData) =>
      RequestCodec.decode(requestBytes).require map {
        case Request(rpcRequest) =>
          val result =
            if (chain.isDefinedAt(rpcRequest)) {
              chain(rpcRequest)(clientData)
            } else {
              log.error("Unsupported request {}", rpcRequest)
              Future.successful(Error(CommonErrors.UnsupportedRequest))
            }

          result
            .map(_.fold(err => err, ok => ok))
            .map(result => RpcResponse(messageId, RpcResultCodec.encode(result).require))
            .pipeTo(sender())
        case _ =>
          Future.successful(CommonErrors.UnsupportedRequest)
      }
  }
}
