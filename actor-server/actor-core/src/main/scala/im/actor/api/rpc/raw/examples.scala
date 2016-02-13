package im.actor.api.rpc.raw

import im.actor.api.rpc.collections.{ ApiInt64Value, ApiStringValue, ApiInt32Value, ApiArrayValue }

object examples {

  import RawValueParser._

  case class Foo(a: Int, b: String, c: Long)

  case class Bar(a: String)

  def doIt() = {
    val fooArray = ApiArrayValue(Vector(
      ApiInt32Value(22),
      ApiStringValue("hello"),
      ApiInt64Value(33L)
    ))

    val barArray = ApiArrayValue(Vector(
      ApiStringValue("here we go!")
    ))

    val foo = RawValueParser.parse[Foo](fooArray)
    val bar = RawValueParser.parse[Bar](barArray)

    println(s"Parsing foo array to: $foo")
    println(s"Parsing bar array to: $bar")
  }
}
