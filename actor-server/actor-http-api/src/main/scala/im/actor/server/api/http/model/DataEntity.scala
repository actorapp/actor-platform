package im.actor.server.api.http.model

import play.api.libs.json.{ JsArray, Json, JsObject, Writes }

final case class DataEntity[A](data: A) {
  def toJson(implicit writes: Writes[A]) = JsObject(Map("data" → Json.toJson(data)))
}

final case class DataEntities[A](datas: Seq[A]) {
  def toJson(implicit writes: Writes[A]) = JsObject(Map("data" → JsArray(datas map (Json.toJson(_)))))
}