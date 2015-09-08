package im.actor.server.dashboard.controllers.utils.json

import scala.concurrent.forkjoin.ThreadLocalRandom

import com.github.tminglei.slickpg.LTree
import play.api.libs.functional.syntax._
import play.api.libs.json._

import im.actor.server.dashboard.controllers.utils.NestedDept
import im.actor.server.dashboard.controllers.utils.json.Common._
import im.actor.server.models

object DepartmentsJsonImplicits {

  implicit val userWrites = Common.userWrites

  implicit val ltreeWrites = new Writes[LTree] {
    def writes(tree: LTree) = JsNumber(tree.value.mkString.toInt)
  }

  implicit val departmentWrites: Writes[NestedDept] = (
    (__ \ "id").write[LTree] and
    (__ \ "internal-id").write[String] and
    (__ \ "title").write[String] and
    (__ \ "items").lazyWrite(Writes.traversableWrites[NestedDept](departmentWrites))
  )(unlift(NestedDept.unapply))

  implicit val departmentReads: Reads[models.Department] = (
    (JsPath \ "title").read[String](length) and
    (JsPath \ "struct").read[String](length)
  )(makeDepartment _)

  case class DepartmentUpdate(title: String)

  implicit val deptUpdateReads: Reads[DepartmentUpdate] = (JsPath \ "title").read[String](length).map { DepartmentUpdate }

  private def makeDepartment(name: String, struct: String): models.Department = {
    val rnd = ThreadLocalRandom.current()
    models.Department(IdUtils.nextIntId(rnd), name, LTree(struct))
  }

}
