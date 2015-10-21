package im.actor.bots.macros

import scala.annotation.StaticAnnotation
import scala.language.experimental.macros
import scala.language.postfixOps
import scala.reflect.macros.blackbox.Context

final class BotInterface extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro BotInterfaceImpl.impl
}

private object BotInterfaceImpl {
  def impl(c: Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._

    def extractCaseClassFields(clazz: ClassSymbol) = {
      clazz.typeSignature.decls collect {
        case m: MethodSymbol if m.isCaseAccessor ⇒ m //internal.valDef(m)
      } toList
    }

    def extractClassName(clazz: ClassSymbol) = clazz.name.decodedName.toString

    def javaFields(fields: List[MethodSymbol]) = {
      fields.foldLeft((false, List.empty[c.universe.Tree])) {
        case ((hasJavaField, fields), field) ⇒
          field.typeSignature.typeSymbol match {
            case s if s == c.mirror.staticClass("scala.Option") ⇒
              val typ = field.typeSignature.resultType.typeArgs.head
              val jfield = q"""${field.name}: java.util.Optional[$typ]"""
              (true, fields :+ jfield)
            case _ ⇒
              (hasJavaField, fields :+ internal.valDef(field))
          }
      }
    }

    def rqDef(decl: c.universe.ClassSymbol): List[c.universe.Tree] = {
      val rqClassName = extractClassName(decl)
      val methodName = s"request${rqClassName}"
      val method = TermName(methodName)
      val fields = extractCaseClassFields(decl)

      val rqConst =
        if (fields.nonEmpty)
          q"""${decl.companion}(..${fields map (_.name)})"""
        else
          q"""${decl.companion}"""

      val (hasjFields, jfields) = javaFields(fields)

      val sfields = fields map (internal.valDef(_))

      if (hasjFields) {
        List(
          q"""def $method(..$jfields) = request($rqConst)""",
          q"""def $method(..$sfields) = request($rqConst)"""
        )
      } else {
        List(q"""def $method(..$sfields) = request($rqConst)""")
      }
    }

    val requests = c.mirror.staticClass("im.actor.bots.BotMessages.RequestBody").knownDirectSubclasses collect {
      case cs: ClassSymbol ⇒ cs
    }

    val updates = c.mirror.staticClass("im.actor.bots.BotMessages.UpdateBody").knownDirectSubclasses collect {
      case cs: ClassSymbol ⇒ cs
    }

    val (updCallbacks, updDefs) = updates map { decl ⇒
      val methodName = s"on${decl.name.decodedName.toString}"
      val method = TermName(methodName)
      ((decl, method), q"def $method(upd: $decl): Unit")
    } unzip

    val requestDef =
      q"""
        def request[T <: im.actor.bots.BotMessages.RequestBody](body: T): scala.concurrent.Future[body.Response]
        """

    val rqDefs = requests flatMap (rqDef(_))

    val updCases = updCallbacks map {
      case (upd, method) ⇒
        cq"""
           u: $upd => $method(u)
          """
    }

    val onUpdateDef =
      q"""final def onUpdate(upd: im.actor.bots.BotMessages.UpdateBody): Unit = {
            upd match {
              case ..$updCases
            }
          }"""

    annottees map (_.tree) toList match {
      case q"$mods class $name(..$args) extends ..$parents { ..$body }" :: Nil ⇒
        c.Expr[Any](
          q"""
               $mods class $name(..$args) extends ..$parents {
                 import scala.compat.java8.OptionConverters._
                 import scala.language.implicitConversions
                 implicit def optionalToOption[A](optional: java.util.Optional[A]): Option[A] = optional.asScala
                 $requestDef
                 ..$rqDefs
                 ..$updDefs
                 $onUpdateDef
                 ..$body
               }
             """
        )
      case unexpected ⇒ c.abort(c.enclosingPosition, "Invalid annottee")
    }
  }
}
