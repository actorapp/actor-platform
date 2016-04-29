package im.actor.server.file.local.http.fix

import akka.NotUsed
import akka.stream.scaladsl.Flow
import akka.stream.stage.{ Context, StatefulStage, SyncDirective }
import akka.util.ByteString

//TODO: remove as soon, as https://github.com/akka/akka/issues/20338 get fixed
object StreamUtilsFix {

  def sliceBytesTransformer(start: Long, length: Long): Flow[ByteString, ByteString, NotUsed] = {
    val transformer = new StatefulStage[ByteString, ByteString] {

      def skipping = new State {
        var toSkip = start

        override def onPush(element: ByteString, ctx: Context[ByteString]): SyncDirective =
          if (element.length < toSkip) {
            // keep skipping
            toSkip -= element.length
            ctx.pull()
          } else {
            become(taking(length))
            // toSkip <= element.length <= Int.MaxValue
            current.onPush(element.drop(toSkip.toInt), ctx)
          }
      }

      def taking(initiallyRemaining: Long) = new State {
        var remaining: Long = initiallyRemaining

        override def onPush(element: ByteString, ctx: Context[ByteString]): SyncDirective = {
          val data = element.take(math.min(remaining, Int.MaxValue).toInt)
          remaining -= data.size
          if (remaining <= 0) ctx.pushAndFinish(data)
          else ctx.push(data)
        }
      }

      override def initial: State = if (start > 0) skipping else taking(length)
    }
    Flow[ByteString].transform(() ⇒ transformer).named("sliceBytes")
  }

}
