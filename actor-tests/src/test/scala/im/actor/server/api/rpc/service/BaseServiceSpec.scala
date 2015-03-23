package im.actor.server.api.rpc.service

import com.typesafe.config.ConfigFactory
import org.specs2.matcher.ThrownExpectations
import org.specs2.specification.core.Fragments

import im.actor.server.SqlSpecHelpers
import im.actor.util.testing._

trait BaseServiceSpec
    extends ActorSpecification
    with ThrownExpectations
    with SqlSpecHelpers
    with ServiceSpecHelpers
    with HandlerMatchers {
  implicit lazy val (ds, db) = migrateAndInitDb()

  override def map(fragments: => Fragments) =
    super.map(fragments) ^ step(closeDb())

  def closeDb() = {
    ds.close()
  }
}
