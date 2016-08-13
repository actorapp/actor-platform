package im.actor.server.group

object PushTexts {
  def added(gt: GroupType, name: String) =
    if (gt.isChannel)
      s"$name invitó al canal"
    else
      s"$name invitó al grupo"

  def kicked(gt: GroupType, name: String) =
    if (gt.isChannel)
      s"$name expulsado del canal"
    else
      s"$name expulsado del grupo"

  def left(gt: GroupType, name: String) =
    if (gt.isChannel)
      s"$name elimino el canal"
    else
      s"$name elimino el grupo"

  def invited(gt: GroupType) =
    if (gt.isChannel)
      "Se le invita a un canal"
    else
      "Se le invita al grupo"

  def titleChanged(gt: GroupType) =
    if (gt.isChannel)
      "Canal título cambió"
    else
      "Grupo título cambió"

  def topicChanged(gt: GroupType) =
    if (gt.isChannel)
      "Canal tema cambió"
    else
      "Grupo tema cambió"
}
