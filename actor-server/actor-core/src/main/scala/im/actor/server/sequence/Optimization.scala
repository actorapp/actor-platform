package im.actor.server.sequence

import com.google.protobuf.ByteString
import im.actor.api.rpc.counters.UpdateCountersChanged
import im.actor.api.rpc.groups._
import im.actor.api.rpc.sequence.{ ApiUpdateOptimization, UpdateEmptyUpdate }
import im.actor.server.model.SerializedUpdate

object Optimization {
  type UpdateHeader = Int
  type Func = SerializedUpdate ⇒ SerializedUpdate
  val EmptyFunc: Func = identity[SerializedUpdate]

  private val emptyUpdate = SerializedUpdate(
    header = UpdateEmptyUpdate.header,
    body = ByteString.copyFrom(UpdateEmptyUpdate.toByteArray)
  )

  private val excludeUpdates: Map[ApiUpdateOptimization.Value, Set[UpdateHeader]] = Map(
    ApiUpdateOptimization.STRIP_COUNTERS → Set(UpdateCountersChanged.header),
    ApiUpdateOptimization.GROUPS_V2 → Set(
      UpdateGroupInviteObsolete.header,
      UpdateGroupUserInvitedObsolete.header,
      UpdateGroupUserLeaveObsolete.header,
      UpdateGroupUserKickObsolete.header,
      UpdateGroupMembersUpdateObsolete.header,
      UpdateGroupTitleChangedObsolete.header,
      UpdateGroupTopicChangedObsolete.header,
      UpdateGroupAboutChangedObsolete.header,
      UpdateGroupAvatarChangedObsolete.header
    )
  )

  def apply(optimizations: Seq[Int]): Func =
    Function.chain(optimizations map { opt ⇒
      excludeUpdates.get(ApiUpdateOptimization(opt)) map { exclude ⇒ upd: SerializedUpdate ⇒ if (exclude contains upd.header) emptyUpdate else upd
      } getOrElse EmptyFunc
    })
}
