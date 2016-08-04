package im.actor.server.frontend

import akka.stream.stage.{ Context, PushStage, SyncDirective }
import com.github.ghik.silencer.silent
import im.actor.server.mtproto.transport.{ Handshake, TransportPackage }

@silent
private[frontend] final class PackageCheckStage extends PushStage[TransportPackage, TransportPackage] {

  private trait State

  private case object AwaitHandshake extends State

  private case object Passing extends State

  // FIXME: check package index

  private var state: State = AwaitHandshake
  private var lastPackageIndex: Int = -1

  override def onPush(elem: TransportPackage, ctx: Context[TransportPackage]): SyncDirective = {
    state match {
      case AwaitHandshake ⇒
        elem.body match {
          case _: Handshake ⇒
            this.state = Passing
            this.lastPackageIndex = elem.index
            ctx.push(elem)
          case unexpected ⇒ ctx.fail(new Exception(s"Expected Handshake but got ${unexpected}"))
        }
      case Passing ⇒
        this.lastPackageIndex = elem.index
        ctx.push(elem)
    }
  }
}

