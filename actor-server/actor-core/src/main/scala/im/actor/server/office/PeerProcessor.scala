package im.actor.server.office

trait PeerProcessor[State <: ProcessorState, Event <: AnyRef] extends Processor[State, Event]