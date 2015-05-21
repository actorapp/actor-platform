package im.actor.server.ilectro

import java.util.UUID

import scala.concurrent.Future

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorFlowMaterializer
import com.typesafe.config.ConfigFactory
import slick.dbio
import slick.dbio.Effect.{ Transactional, Write }
import slick.driver.PostgresDriver.api._

import im.actor.server.ilectro.results.{ Banner, Errors }
import im.actor.server.models.ilectro.{ ILectroUser, Interest }
import im.actor.server.persist

class ILectro(implicit system: ActorSystem) {
  private implicit val ec = system.dispatcher
  private implicit val meterializer = ActorFlowMaterializer()
  private implicit val http = Http()

  private implicit val config = ILectroConfig(ConfigFactory.load().getConfig("actor-server.ilectro"))

  private val users = new Users()
  private val lists = new Lists()

  def deleteInterest(user: ILectroUser, interests: Vector[Int]): dbio.DBIOAction[Vector[Either[Errors, Unit]], NoStream, Write with Effect with Transactional] = {
    DBIO.sequence(
      for {
        result ← interests.map { interest ⇒
          (for {
            _ ← persist.ilectro.UserInterest.delete(user.userId, interest)
            resp ← DBIO.from(users.deleteInterest(user.uuid, interest))
          } yield resp).transactionally
        }
      } yield result
    )
  }

  def addInterests(user: ILectroUser, interests: Vector[Int]): dbio.DBIOAction[Right[Errors, Unit], NoStream, Effect with Effect with Write with Transactional] = {
    (for {
      _ ← DBIO.sequence(interests.map(id ⇒ persist.ilectro.UserInterest.create(user.userId, id)))
      resp ← DBIO.from(users.addInterests(user.uuid, interests.toList)) flatMap {
        case Left(e)      ⇒ DBIO.failed(new Exception(e.errors))
        case r @ Right(_) ⇒ DBIO.successful(r)
      }
    } yield resp).transactionally
  }

  def getBanners(userUuid: UUID): Future[Banner] = {
    users.getBanners(userUuid) map {
      case Right(banners) ⇒ banners
      case Left(e)        ⇒ throw new Exception(s"Failed to get banners: $e")
    }
  }

  def getAndPersistInterests()(implicit db: Database): Future[Int] = {
    lists.getInterests() flatMap {
      case Right(interests) ⇒ persistInterests(interests)
      case Left(e) ⇒
        throw new Exception(s"Failed to load interests: ${e}")
    }
  }

  def persistInterests(interests: List[Interest])(implicit db: Database): Future[Int] = {
    db.run {
      persist.ilectro.Interest.createOrUpdate(interests)
    } map (_.sum)
  }
}
