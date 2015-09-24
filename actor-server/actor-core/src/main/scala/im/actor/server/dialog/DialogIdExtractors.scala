package im.actor.server.dialog

object DialogIdExtractors {

  def getPrivateDialogId(container: DialogIdContainer) = container match {
    case DialogIdContainer(id) if id.isPrivat ⇒ container.getPrivat
    case _                                    ⇒ throw new Exception(s"Got wrong dialog id type, expected PrivateDialogId, got: $container")
  }

  def getGroupDialogId(container: DialogIdContainer) = container match {
    case DialogIdContainer(id) if id.isGroup ⇒ container.getGroup
    case _                                   ⇒ throw new Exception(s"Got wrong dialog id type, expected GroupDialogId, got: $container")
  }
}

