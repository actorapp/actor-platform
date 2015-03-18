package im.actor.server.api.rpc.service

import org.specs2.matcher.ThrownExpectations

import im.actor.server.SqlSpecHelpers
import im.actor.util.testing._

trait BaseServiceSpec
    extends ActorSpecification
    with ThrownExpectations
    with SqlSpecHelpers
    with ServiceSpecHelpers
    with HandlerMatchers {

}
