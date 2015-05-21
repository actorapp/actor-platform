package im.actor.server.ilectro

import java.util.UUID

import scala.concurrent.Future

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorFlowMaterializer
import com.typesafe.config.ConfigFactory
import slick.driver.PostgresDriver.api._

import im.actor.server.models.ilectro.Interest
import im.actor.server.persist

class ILectro(implicit system: ActorSystem) {
  private implicit val ec = system.dispatcher
  private implicit val meterializer = ActorFlowMaterializer()
  private implicit val http = Http()

  private implicit val config = ILectroConfig(ConfigFactory.load().getConfig("actor-server.ilectro"))

  val users = new Users()
  val lists = new Lists()

  def getBanners(userUuid: UUID) = {
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
