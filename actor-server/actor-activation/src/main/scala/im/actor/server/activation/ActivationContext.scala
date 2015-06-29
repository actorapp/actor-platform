package im.actor.server.activation

import im.actor.server.activation.Activation.Code

trait ActivationContext {
  def send(authId: Long, code: Code): Unit
}

class DummyActivationContext extends ActivationContext {
  override def send(authId: Long, code: Code): Unit = {}
}
