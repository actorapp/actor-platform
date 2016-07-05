package im.actor.server

import akka.actor.ActorSystem
import akka.event.Logging
import com.google.protobuf.{ ByteString, CodedInputStream }
import im.actor.api.rpc.{ ClientData, Update }
import im.actor.server.model.SerializedUpdate
import im.actor.server.sequence.{ SeqState, SeqUpdatesExtension }
import im.actor.util.log.AnyRefLogSource
import org.scalatest.Matchers
import org.scalatest.concurrent.ScalaFutures

import scala.annotation.tailrec
import scala.concurrent.Future
import scala.reflect._
import scala.reflect.runtime._
import scala.util.{ Failure, Success, Try }

trait SeqUpdateMatchers extends Matchers with ScalaFutures with AnyRefLogSource {
  protected implicit val system: ActorSystem

  private val DefaultRetryCount = 5
  private val DefaultRetryInterval: Long = 800

  private val log = Logging(system, this)

  private type UpdateClass = Class[_ <: Update]

  val emptyState: SeqState = SeqState(0, ByteString.EMPTY)

  def mkSeqState(seq: Int, commonState: Array[Byte]): SeqState = SeqState(seq, ByteString.copyFrom(commonState))

  def getCurrentState(implicit clientData: ClientData): SeqState =
    whenReady(SeqUpdatesExtension(system).getSeqState(clientData.optUserId.get, clientData.authId))(identity)

  def expectUpdate[Upd <: Update: ClassTag](clazz: Class[Upd])(check: Upd ⇒ Any)(implicit client: ClientData): SeqState =
    expectUpdate(emptyState, clazz)(check)

  def expectUpdate[Upd <: Update: ClassTag](state: SeqState, clazz: Class[Upd])(check: Upd ⇒ Any)(implicit client: ClientData): SeqState = {
    matchUpdates(state) { updates ⇒
      val optUpdate = updates find (_.header == extractHeader(clazz))
      withClue(s"There was no update of type ${clazz.getSimpleName} in difference") {
        optUpdate shouldBe defined
      }
      check(extractUpdate(clazz, optUpdate.get))
    }
  }

  def expectUpdates(updates: UpdateClass*)(check: PartialFunction[Seq[Update], Any])(implicit client: ClientData): SeqState =
    expectUpdates(emptyState, updates: _*)(check)

  def expectUpdates(state: SeqState, updates: UpdateClass*)(check: PartialFunction[Seq[Update], Any])(implicit client: ClientData): SeqState =
    expectUpdatesAbstract(state, updates)(check)(
      { (dbUpdatesHeaders, updatesHeaders) ⇒ dbUpdatesHeaders containsSlice updatesHeaders },
      { (dbUpdatesNames, updatesNames) ⇒
        s"""Error: did not get expected updates in given order.
            |expected updates: $updatesNames
            |actual updates: $dbUpdatesNames
      """.stripMargin
      }
    )

  def expectUpdatesUnordered(updates: UpdateClass*)(check: PartialFunction[Seq[Update], Any])(implicit client: ClientData): SeqState =
    expectUpdatesUnordered(emptyState, updates: _*)(check)

  def expectUpdatesUnordered(state: SeqState, updates: UpdateClass*)(check: PartialFunction[Seq[Update], Any])(implicit client: ClientData): SeqState =
    expectUpdatesAbstract(state, updates)(check)(
      { (dbUpdatesHeaders, updatesHeaders) ⇒ updatesHeaders.toSet subsetOf dbUpdatesHeaders.toSet },
      { (dbUpdatesNames, updatesNames) ⇒
        s"""Error: did not get expected updates.
            |expected updates: $updatesNames
            |actual updates: $dbUpdatesNames
      """.stripMargin
      }
    )

  def expectUpdatesOnly(updates: UpdateClass*)(check: PartialFunction[Seq[Update], Any])(implicit client: ClientData): SeqState =
    expectUpdatesOnly(emptyState, updates: _*)(check)

  def expectUpdatesOnly(state: SeqState, updates: UpdateClass*)(check: PartialFunction[Seq[Update], Any])(implicit client: ClientData): SeqState =
    expectUpdatesAbstract(state, updates)(check)(
      { (dbUpdatesHeaders, updatesHeaders) ⇒ dbUpdatesHeaders == updatesHeaders },
      { (dbUpdatesNames, updatesNames) ⇒
        s"""Error: did not get expected updates ONLY in given order.
            |expected updates: $updatesNames
            |actual updates: $dbUpdatesNames
    """.stripMargin
      }
    )

  //todo: make timeout configurable
  def expectNoUpdate[Upd <: Update: ClassTag](state: SeqState, update: Class[Upd])(implicit client: ClientData): Unit = {
    Thread.sleep(4000)
    val updateHeader = extractHeader(update)
    whenReady(findSeqUpdateAfter(state)) {
      case (updates, _) ⇒
        if (updates.exists(_.header == updateHeader)) fail(s"There should be no update of type: ${update.getSimpleName}")
    }
  }

  def emptyCheck: PartialFunction[Seq[Update], Any] = {
    case _ ⇒
  }

  private def expectUpdatesAbstract[Upd <: Update: ClassTag](state: SeqState, updates: Seq[UpdateClass])(check: PartialFunction[Seq[Update], Any])(containsCheck: (Seq[Int], Seq[Int]) ⇒ Boolean, errorMessage: (String, String) ⇒ String)(implicit client: ClientData): SeqState = {
    matchUpdates(state) { dbUpdates ⇒
      val headersToUpdates = updates map (u ⇒ extractHeader(u) → u)
      val updatesMap: Map[Int, UpdateClass] = headersToUpdates.toMap
      val updatesHeaders = headersToUpdates map (_._1)
      val updatesNames = headersToUpdates map {
        case (h, u) ⇒ s"${u.getSimpleName}{${h}}"
      } mkString ", "

      val dbUpdatesHeaders = dbUpdates map (_.header)
      val dbUpdatesNames = dbUpdatesHeaders mkString ", "

      withClue(errorMessage(dbUpdatesNames, updatesNames)) { containsCheck(dbUpdatesHeaders, updatesHeaders) shouldEqual true }
      val parsedUpdates: Iterable[Seq[Upd]] = dbUpdates.foldLeft(Map.empty[Int, Seq[Upd]].withDefaultValue(Seq.empty[Upd])) { (acc, el) ⇒
        updatesMap.get(el.header) map { clazz ⇒
          acc.updated(el.header, acc(el.header) :+ extractUpdate(clazz, el))
        } getOrElse acc
      }.values
      parsedUpdates foreach (check orElse emptyCheck)
    }
  }

  private def extractHeader[U <: Update: ClassTag](clazz: Class[U]): Int = callCompanionMethod[Int](clazz, "header")

  private def extractUpdate[U <: Update: ClassTag](clazz: UpdateClass, update: SerializedUpdate): U = {
    val runtimeMirror = universe.runtimeMirror(getClass.getClassLoader)

    val moduleSymbol = currentMirror.moduleSymbol(clazz)
    val moduleMirror = currentMirror.reflectModule(moduleSymbol)

    val parseFromAlternatives = moduleMirror.symbol.info.decl(universe.TermName("parseFrom")).asTerm.alternatives
    val parseFromFiltered = parseFromAlternatives filter { method ⇒
      method.asMethod.info.paramLists match {
        case List(List(x)) if x.info =:= universe.typeOf[CodedInputStream] ⇒ true
        case _ ⇒ false
      }
    }

    val parseFrom = parseFromFiltered.headOption map (_.asMethod) getOrElse fail(s"Could not find parseFrom method in $clazz")
    val objectMirror = runtimeMirror.reflect(moduleMirror.instance)
    val updateEither = objectMirror.reflectMethod(parseFrom).apply(CodedInputStream.newInstance(update.body.toByteArray)).asInstanceOf[Either[String, U]]
    updateEither should matchPattern {
      case Right(_) ⇒
    }
    updateEither.right.get
  }

  //could not be applied to extract parseFrom method from Updates
  private def callCompanionMethod[Result](fromClass: Class[_], methodName: String, args: Any*): Result = {
    val runtimeMirror = universe.runtimeMirror(getClass.getClassLoader)

    val moduleSymbol = currentMirror.moduleSymbol(fromClass)
    val moduleMirror = currentMirror.reflectModule(moduleSymbol)

    val method = moduleMirror.symbol.info.decl(universe.TermName(methodName)).asMethod

    val objectMirror = runtimeMirror.reflect(moduleMirror.instance)
    objectMirror.reflectMethod(method).apply(args).asInstanceOf[Result]
  }

  private def matchUpdates(state: SeqState)(check: Seq[SerializedUpdate] ⇒ Any)(implicit client: ClientData): SeqState =
    repeatAfterSleep(DefaultRetryCount) {
      whenReady(findSeqUpdateAfter(state)) {
        case (updates, newState) ⇒
          check(updates)
          newState
      }
    }

  private def findSeqUpdateAfter(seqState: SeqState)(implicit client: ClientData): Future[(Seq[SerializedUpdate], SeqState)] =
    SeqUpdatesExtension(system)
      .getDifference(
        client.authData.get.userId,
        seqState.seq,
        seqState.state.toByteArray,
        client.authId,
        client.authData.get.authSid,
        Long.MaxValue
      )
      .map { diff ⇒
        diff.updates → mkSeqState(diff.clientSeq, diff.commonState)
      }(system.dispatcher)

  //TODO: move it to separate. decouple from updates.
  @tailrec
  final def repeatAfterSleep[T](times: Int)(f: ⇒ T): T = {
    Try(f) match {
      case Success(result) ⇒
        log.debug("Update successfully matched")
        result
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
