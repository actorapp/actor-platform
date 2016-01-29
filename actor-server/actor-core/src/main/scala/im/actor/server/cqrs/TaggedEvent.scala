package im.actor.server.cqrs

trait Event

trait TaggedEvent extends Event {
  def tags: Set[String]
}