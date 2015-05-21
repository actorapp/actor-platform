package im.actor.server.ilectro

import scala.collection.immutable.SortedMap
import scala.concurrent._
import scala.concurrent.duration._

import akka.actor.ActorSystem
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model.HttpMethods.GET
import akka.http.scaladsl.model._
import akka.stream.ActorFlowMaterializer
import upickle._

import im.actor.server.ilectro.Common._
import im.actor.server.ilectro.results._

private[ilectro] class Lists(implicit
  system: ActorSystem,
                             executionContext: ExecutionContext,
                             materializer:     ActorFlowMaterializer,
                             http:             HttpExt,
                             config:           ILectroConfig) {

  private implicit val authToken = config.authToken
  private val baseUrl = config.baseUrl

  private val resourceName = "interests"

  def getInterests(): Future[Either[Errors, List[Interest]]] = processRequest(
    request = HttpRequest(
      method = GET,
      uri = s"$baseUrl/$resourceName"
    ),
    success = (entity) ⇒
      entity.toStrict(5.seconds).map { e ⇒
        val body = e.data.decodeString("utf-8")
        Right(read[List[Interest]](body))
      },
    failure = defaultFailure
  )

  def nestInterestsViaMap(list: List[Interest]): List[ReadyInterest] = {
    val map = list.groupBy(_.level).map { case (level, sublist) ⇒ level → sublist.groupBy(_.parentId) }
    val sorted = SortedMap(map.toArray: _*)
    sorted.foldLeft(List.empty[ReadyInterest]) {
      case (acc, (level, parentIdToInterests)) ⇒
        acc match {
          //only one level of nesting
          case h :: t ⇒ acc.map(ri ⇒ parentIdToInterests.get(ri.interest.id).map(interests ⇒ ri.copy(children = interests.map(ReadyInterest(_)))).getOrElse(ri))
          case Nil    ⇒ parentIdToInterests.toList.flatMap { case (_, interests) ⇒ interests.map(ReadyInterest(_)) }
        }
    }
  }

  def nestInterests(list: Seq[Interest]): List[ReadyInterest] = {
    def run(from: List[Interest], acc: List[ReadyInterest]): List[ReadyInterest] = {
      from match {
        case h :: t ⇒
          println(s"parent id ${h.parentId}, id: ${h.id}, level: ${h.level}")
          if (acc.forall(_.interest.level == h.level)) {
            run(t, acc :+ ReadyInterest(h))
          } else {
            acc.zipWithIndex.find(_._1.interest.id == h.parentId) match {
              case Some((parent, index)) ⇒
                run(t, acc.updated(index, parent.copy(children = parent.children :+ ReadyInterest(h))))
              case None ⇒
                //problem is here - rewrites existing accumulator, only last level lefts
                run(t, acc.flatMap(e ⇒ run(List(h), e.children)))
            }
          }
        case _ ⇒ acc
      }
    }
    run(list.toList.sortBy(_.level), List())
  }

}
