package im.actor.server

import akka.actor.ActorSystem
import akka.event.Logging
import com.google.protobuf.CodedInputStream
import im.actor.api.rpc.{ ClientData, Update }
import im.actor.server.db.DbExtension
import im.actor.server.model.{ SeqUpdate, SerializedUpdate }
import im.actor.server.persist.sequence.UserSequenceRepo
import im.actor.server.sequence.SeqUpdatesExtension
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
  import system.dispatcher

  private val DefaultRetryCount = 5
  private val DefaultRetryInterval: Long = 800

  private val log = Logging(system, this)

  type UpdateClass = Class[_ <: Update]

  def getCurrentSeq(implicit clientData: ClientData): Int =
    whenReady(SeqUpdatesExtension(system).getSeqState(clientData.optUserId.get) map (_.seq))(identity)

  def expectUpdate[Upd <: Update: ClassTag](clazz: Class[Upd])(check: Upd ⇒ Any)(implicit client: ClientData): Int =
    expectUpdate(seq = 0, clazz)(check)

  def expectUpdate[Upd <: Update: ClassTag](seq: Int, clazz: Class[Upd])(check: Upd ⇒ Any)(implicit client: ClientData): Int = {
    matchUpdates(seq) { dbUpdates ⇒
      val optUpdate = dbUpdates find (_.header == extractHeader(clazz))
      withClue(s"There was no update of type ${clazz.getSimpleName} in difference") {
        optUpdate shouldBe defined
      }
      check(extractUpdate(clazz, optUpdate.get))
    }
  }

  def expectUpdates(updates: UpdateClass*)(check: PartialFunction[Seq[Update], Any])(implicit client: ClientData): Int =
    expectUpdates(seq = 0, updates: _*)(check)

  def expectUpdates(seq: Int, updates: UpdateClass*)(check: PartialFunction[Seq[Update], Any])(implicit client: ClientData): Int =
    expectUpdatesAbstract(seq, updates)(check)(
      { (dbUpdatesHeaders, updatesHeaders) ⇒ dbUpdatesHeaders containsSlice updatesHeaders },
      { (dbUpdatesNames, updatesNames) ⇒
        s"""Error: did not get expected updates in given order.
            |expected updates: $updatesNames
            |actual updates: $dbUpdatesNames
      """.stripMargin
      }
    )

  def expectUpdatesUnordered(updates: UpdateClass*)(check: PartialFunction[Seq[Update], Any])(implicit client: ClientData): Int =
    expectUpdatesUnordered(seq = 0, updates: _*)(check)

  def expectUpdatesUnordered(seq: Int, updates: UpdateClass*)(check: PartialFunction[Seq[Update], Any])(implicit client: ClientData): Int =
    expectUpdatesAbstract(seq, updates)(check)(
      { (dbUpdatesHeaders, updatesHeaders) ⇒ updatesHeaders.toSet subsetOf dbUpdatesHeaders.toSet },
      { (dbUpdatesNames, updatesNames) ⇒
        s"""Error: did not get expected updates.
            |expected updates: $updatesNames
            |actual updates: $dbUpdatesNames
      """.stripMargin
      }
    )

  def expectUpdatesOnly(updates: UpdateClass*)(check: PartialFunction[Seq[Update], Any])(implicit client: ClientData): Int =
    expectUpdatesOnly(seq = 0, updates: _*)(check)

  def expectUpdatesOnly(seq: Int, updates: UpdateClass*)(check: PartialFunction[Seq[Update], Any])(implicit client: ClientData): Int =
    expectUpdatesAbstract(seq, updates)(check)(
      { (dbUpdatesHeaders, updatesHeaders) ⇒ dbUpdatesHeaders == updatesHeaders },
      { (dbUpdatesNames, updatesNames) ⇒
        s"""Error: did not get expected updates ONLY in given order.
            |expected updates: $updatesNames
            |actual updates: $dbUpdatesNames
    """.stripMargin
      }
    )

  //todo: make timeout configurable
  def expectNoUpdate[Upd <: Update: ClassTag](seq: Int, update: Class[Upd])(implicit client: ClientData): Unit = {
    Thread.sleep(4000)
    val updateHeader = extractHeader(update)
    whenReady(findSeqUpdateAfter(seq)) { updates ⇒
      val authSid = client.authData.get.authSid
      if (updates.map(u ⇒ u.getMapping.custom.getOrElse(authSid, u.getMapping.getDefault).header)
        .contains(updateHeader)) fail(s"There should be no update of type: ${update.getSimpleName}")
    }
  }

  def emptyCheck: PartialFunction[Seq[Update], Any] = {
    case _ ⇒
  }

  private def expectUpdatesAbstract[Upd <: Update: ClassTag](seq: Int, updates: Seq[UpdateClass])(check: PartialFunction[Seq[Update], Any])(containsCheck: (Seq[Int], Seq[Int]) ⇒ Boolean, errorMessage: (String, String) ⇒ String)(implicit client: ClientData): Int = {
    matchUpdates(seq) { dbUpdates ⇒
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

  private def matchUpdates(seq: Int)(check: Seq[SerializedUpdate] ⇒ Any)(implicit client: ClientData): Int =
    repeatAfterSleep(DefaultRetryCount) {
      whenReady(findSeqUpdateAfter(seq)) { updates ⇒
        val authSid = client.authData.get.authSid

        val serUpdates = updates map { update ⇒
          update.getMapping.custom.getOrElse(authSid, update.getMapping.getDefault)
        }

        check(serUpdates)
        updates.lastOption map (_.seq) getOrElse fail("Retrieved empty sequence")
      }
    }

  private def findSeqUpdateAfter(seq: Int)(implicit client: ClientData): Future[Seq[SeqUpdate]] =
    SeqUpdatesExtension(system)
      .getDifference(client.authData.get.userId, seq, client.authData.get.authSid, Long.MaxValue)
      .map(_._1)(system.dispatcher)

  @tailrec
  private def repeatAfterSleep[T](times: Int)(f: ⇒ T): T = {
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
