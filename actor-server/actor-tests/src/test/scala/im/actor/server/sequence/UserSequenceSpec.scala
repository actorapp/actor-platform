package im.actor.server.sequence

import akka.pattern.ask
import akka.testkit._
import com.google.protobuf.ByteString
import com.typesafe.config._
import im.actor.api.rpc.contacts.UpdateContactsAdded
import im.actor.server._
import im.actor.server.model.{ SerializedUpdate, UpdateMapping }
import im.actor.server.sequence.UserSequenceCommands.{ DeliverUpdate, Envelope }
import org.scalatest.time.{ Seconds, Span }

import scala.concurrent.duration._

final class UserSequenceSpec extends BaseAppSuite(
  ActorSpecification.createSystem(
    ConfigFactory.parseString(""" push.seq-updates-manager.receive-timeout = 1 second """)
  )
) with ServiceSpecHelpers with ImplicitAuthService with ImplicitSessionRegion {
  behavior of "SeqUpdatesManager"

  it should "increment seq on update push" in e1

  it should "not reply with seq of the ongoing update (concurrency problem)" in e2

  override implicit def patienceConfig: PatienceConfig =
    new PatienceConfig(timeout = Span(10, Seconds))

  private lazy val seqUpdExt = SeqUpdatesExtension(system)
  val region = seqUpdExt.region
  val probe = TestProbe()

  def e1() = {
    val (user, _, _, _) = createUser()
    val update = UpdateContactsAdded(Vector(1, 2, 3))
    val deliverEnv = Envelope(user.id).withDeliverUpdate(
      DeliverUpdate(
        mapping = Some(UpdateMapping(Some(SerializedUpdate(
          header = update.header,
          body = ByteString.copyFrom(update.toByteArray),
          userIds = update._relatedUserIds,
          groupIds = update._relatedGroupIds
        ))))
      )
    )

    {
      probe.send(region.ref, deliverEnv)
      val msg = probe.receiveOne(5.seconds).asInstanceOf[SeqState]
      msg.seq should ===(1)
    }

    {
      probe.send(region.ref, deliverEnv)
      val msg = probe.receiveOne(1.second).asInstanceOf[SeqState]
      msg.seq should ===(2)
    }

    probe.expectNoMsg(3.seconds)

    {
      probe.send(region.ref, deliverEnv)
      val msg = probe.receiveOne(1.second).asInstanceOf[SeqState]
      msg.seq should ===(3)
    }

    for (a ← 1 to 600)
      probe.send(region.ref, deliverEnv)

    probe.receiveN(600, 5.seconds) // seq = 603
    probe.expectNoMsg(4.seconds)

    {
      probe.send(region.ref, deliverEnv)
      val msg = probe.receiveOne(5.seconds).asInstanceOf[SeqState]
      msg.seq should ===(604)
    }
  }

  def e2() = {
    val (user, _, _, _) = createUser()
    val update = UpdateContactsAdded(Vector(1, 2, 3))
    val deliverEnv = Envelope(user.id).withDeliverUpdate(
      DeliverUpdate(
        mapping = Some(UpdateMapping(Some(SerializedUpdate(
          header = update.header,
          body = ByteString.copyFrom(update.toByteArray),
          userIds = update._relatedUserIds,
          groupIds = update._relatedGroupIds
        ))))
      )
    )

    val futures = for (i ← 1 to 500) yield {
      val f = (region.ref ? deliverEnv)
        .mapTo[SeqState]

      (f, i)
    }

    futures foreach {
      case (f, expectedSeq) ⇒
        whenReady(f) { seqstate ⇒
          seqstate.seq shouldEqual expectedSeq
        }
    }
  }
}
