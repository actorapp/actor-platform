package im.actor.server

object Main extends App {
  val kernel = new ApiKernel
  kernel.startup()
}
