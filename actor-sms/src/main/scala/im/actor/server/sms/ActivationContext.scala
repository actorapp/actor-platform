package im.actor.server.sms

trait ActivationContext {
  def send(authId: Long, phoneNumber: Long, code: String): Unit
}

class DummyActivationContext extends ActivationContext {
  override def send(authId: Long, phoneNumber: Long, code: String): Unit = {}
}
