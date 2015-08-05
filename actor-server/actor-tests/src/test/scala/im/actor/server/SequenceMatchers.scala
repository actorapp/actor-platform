package im.actor.server

import akka.actor.ActorSystem
import akka.event.Logging
import com.google.protobuf.CodedInputStream
import im.actor.api.rpc.ClientData
import im.actor.api.rpc.contacts.UpdateContactRegistered
import im.actor.api.rpc.counters.UpdateCountersChanged
import im.actor.api.rpc.groups.{ UpdateGroupTitleChanged, UpdateGroupInvite, UpdateGroupUserInvited }
import im.actor.api.rpc.messaging._
import im.actor.api.rpc.sequence.{ DifferenceUpdate, ResponseGetDifference, SequenceService }
import im.actor.server.util.AnyRefLogSource
import org.scalatest.Matchers
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.{ BeMatcher, MatchResult }
import org.scalatest.Inside._

import scala.annotation.tailrec
import scala.reflect._
import scala.util.{ Failure, Success, Try }
import scalaz.{ -\/, \/- }

trait SequenceMatchers extends Matchers with ScalaFutures with AnyRefLogSource {
  protected implicit val system: ActorSystem //just for logging
  protected implicit val sequenceService: SequenceService

  private val DefaultRetryCount = 5
  private val DefaultRetryInterval: Long = 800

  private val log = Logging(system, this)

  //todo: remove. use single set maybe
  def expectUpdate[T: ClassTag](seq: Int, state: Array[Byte], updateHeader: Int, requireSize: Option[Int] = None)(check: T ⇒ Any)(implicit client: ClientData): Unit = {
    matchUpdates(seq, state) { serviceUpdates ⇒
      requireSize foreach { s ⇒ serviceUpdates should have size s.toLong }
      val optUpdate = serviceUpdates find (_.updateHeader == updateHeader)
      withClue(s"There was no update with header $updateHeader in difference") {
        optUpdate shouldBe defined
      }
      check(parseUpdate[T](optUpdate.get))
    }
  }

  //size bound ordered check
  def expectUpdatesOrdered(unmatchedBehaviour: PartialFunction[(Int, DifferenceUpdate), Any])(seq: Int, state: Array[Byte], orderedHeaders: List[Int])(check: PartialFunction[(Int, DifferenceUpdate), Any])(implicit client: ClientData) = {
    matchUpdates(seq, state) { serviceUpdates ⇒
      val serviceUpdatesHeaders = serviceUpdates.map(_.updateHeader)
      withClue(
        s"""Requested updates were other than updates in difference, or had different order
          |updates headers from service : $serviceUpdatesHeaders
          |requested updates: $orderedHeaders
        """.stripMargin
      ) {
        serviceUpdatesHeaders should contain theSameElementsInOrderAs orderedHeaders
      }
      val updateMap: Map[Int, DifferenceUpdate] = serviceUpdates.map(u ⇒ (u.updateHeader, u)).toMap
      updateMap collect (check orElse unmatchedBehaviour)
    }
  }

  //checks that difference contains all headers that we want to check. Unordered. Does not check size of
  def expectUpdatesUnordered(unmatchedBehaviour: PartialFunction[(Int, DifferenceUpdate), Any])(seq: Int, state: Array[Byte], headers: Set[Int])(check: PartialFunction[(Int, DifferenceUpdate), Any])(implicit client: ClientData) = {
    matchUpdates(seq, state) { serviceUpdates ⇒
      val serviceUpdatesHeaders = serviceUpdates.map(_.updateHeader)
      withClue(
        s"""Did not receive expected update in difference
            |updates headers from service : $serviceUpdatesHeaders
            |requested updates: $headers
        """.stripMargin
      ) {
        (headers diff serviceUpdatesHeaders.toSet) shouldBe empty
      }
      val updateMap: Map[Int, DifferenceUpdate] = serviceUpdates.map(u ⇒ (u.updateHeader, u)).toMap
      updateMap foreach (check orElse unmatchedBehaviour)
    }
  }

  def expectUpdatesUnorderedOnly(unmatchedBehaviour: PartialFunction[(Int, DifferenceUpdate), Any])(seq: Int, state: Array[Byte], headers: List[Int])(check: PartialFunction[(Int, DifferenceUpdate), Any])(implicit client: ClientData) = {
    matchUpdates(seq, state) { serviceUpdates ⇒
      val serviceUpdatesHeaders = serviceUpdates.map(_.updateHeader)
      withClue(
        s"""Requested updates were other than updates in difference (order of elements does not count)
            |updates headers from service : $serviceUpdatesHeaders
            |requested updates: $headers
        """.stripMargin
      ) {
        serviceUpdatesHeaders.sorted should contain theSameElementsInOrderAs headers.sorted
      }
      val updateMap: Map[Int, DifferenceUpdate] = serviceUpdates.map(u ⇒ (u.updateHeader, u)).toMap
      updateMap foreach (check orElse unmatchedBehaviour)
    }
  }

  def failUnmatched: PartialFunction[(Int, DifferenceUpdate), Any] = {
    case _ ⇒ fail("Could not match update during check")
  }

  def ignoreUnmatched: PartialFunction[(Int, DifferenceUpdate), Any] = {
    case _ ⇒
  }

  def parseUpdate[T: ClassTag](diffUpdate: DifferenceUpdate): T = {
    val is = CodedInputStream.newInstance(diffUpdate.update)
    val result = diffUpdate.updateHeader match {
      case UpdateMessageSent.header       ⇒ UpdateMessageSent.parseFrom(is)
      case UpdateCountersChanged.header   ⇒ UpdateCountersChanged.parseFrom(is)
      case UpdateMessage.header           ⇒ UpdateMessage.parseFrom(is)
      case UpdateContactRegistered.header ⇒ UpdateContactRegistered.parseFrom(is)
      case UpdateMessageReceived.header   ⇒ UpdateMessageReceived.parseFrom(is)
      case UpdateMessageReadByMe.header   ⇒ UpdateMessageReadByMe.parseFrom(is)
      case UpdateMessageRead.header       ⇒ UpdateMessageRead.parseFrom(is)
      case UpdateGroupUserInvited.header  ⇒ UpdateGroupUserInvited.parseFrom(is)
      case UpdateGroupInvite.header       ⇒ UpdateGroupInvite.parseFrom(is)
      case UpdateGroupTitleChanged.header ⇒ UpdateGroupTitleChanged.parseFrom(is)
      case _                              ⇒ fail(s"Failed to parse update of given type. Provide header -> update mapping in im.actor.server.SequenceMatchers.parseUpdate")
    }
    inside(result) {
      case Right(x) ⇒ x shouldBe ofType[T]
    }
    result.right.get.asInstanceOf[T]
  }

  private def ofType[T: ClassTag] = BeMatcher { obj: Any ⇒
    val cls = classTag[T].runtimeClass
    MatchResult(
      obj.getClass == cls,
      obj.toString + " was not an instance of " + cls.toString,
      obj.toString + " was an instance of " + cls.toString
    )
  }

  private def matchUpdates(seq: Int, state: Array[Byte])(check: Vector[DifferenceUpdate] ⇒ Any)(implicit client: ClientData) =
    repeatAfterSleep(DefaultRetryCount) { () ⇒
      whenReady(sequenceService.handleGetDifference(seq, state)) { diff ⇒
        inside(diff) {
          case \/-(ResponseGetDifference(_, _, _, updates, _, _)) ⇒ check(updates)
          case -\/(_) ⇒ fail("failed to parse response from sequence service")
        }
      }
    }

  @tailrec
  private def repeatAfterSleep(times: Int)(f: () ⇒ Any): Unit = {
    Try(f()) match {
      case Success(_) ⇒ log.debug("Update successfully matched")
      case Failure(e) ⇒
        if (times == 0) {
          log.error(e, "Failed to match updates")
          fail(e)
        } else {
          Thread.sleep(DefaultRetryInterval)
          val newTimes = times - 1
          log.warning("Updates did not match, will retry up to {} times, exception was: {}", newTimes, e)
          repeatAfterSleep(newTimes)(f)
        }
    }
  }
}
