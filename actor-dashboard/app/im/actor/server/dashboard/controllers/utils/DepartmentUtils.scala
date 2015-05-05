package im.actor.server.dashboard.controllers.utils

import com.github.tminglei.slickpg.LTree

import im.actor.server.models

case class NestedDept(id: LTree, internalId: String, title: String, items: List[NestedDept] = List())

object DepartmentUtils {
  def nestDepartments(list: Seq[models.Department]): List[NestedDept] = {
    def run(from: List[models.Department], acc: List[NestedDept]): List[NestedDept] = {
      from match {
        case h :: t ⇒
          val toNested = NestedDept(h.struct, h.struct.toString, h.name)
          acc.lastOption.map { last ⇒
            val headStruct = h.struct.value
            val tailStruct = last.id.value
            headStruct.startsWith(tailStruct) match {
              case true ⇒
                val updated =
                  if (headStruct.length - tailStruct.length == 1)
                    last.copy(items = last.items :+ toNested)
                  else last.copy(items = run(List(h), last.items))
                run(t, acc.updated(acc.length - 1, updated))
              case false ⇒ run(t, acc :+ toNested)
            }
          } getOrElse run(t, acc :+ toNested)
        case _ ⇒ acc
      }
    }
    run(list.toList, List())
  }
}
