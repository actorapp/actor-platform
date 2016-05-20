package im.actor.api.rpc

import im.actor.api.rpc.messaging.{ ApiDialogGroup, ApiDialogShort }
import im.actor.server.dialog.{ DialogExtension, DialogGroup, DialogInfo }

trait DialogConverters {
  implicit class ExtDialogInfo(info: DialogInfo) {
    lazy val asStruct = ApiDialogShort(info.getPeer.asStruct, info.counter, info.date.toEpochMilli)
  }

  implicit class ExtDialogGroup(group: DialogGroup) {
    lazy val asStruct =
      ApiDialogGroup(
        title = DialogExtension.groupTitle(group.typ),
        key = DialogExtension.groupKey(group.typ),
        dialogs = group.dialogs.toVector map (_.asStruct)
      )
  }
}

object DialogConverters extends DialogConverters