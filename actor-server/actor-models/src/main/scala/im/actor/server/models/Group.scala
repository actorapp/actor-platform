package im.actor.server.models

import org.joda.time.DateTime

case class Group(
  id:            Int,
  creatorUserId: Int,
  accessHash:    Long,
  title:         String,
  isPublic:      Boolean,
  createdAt:     DateTime,
  description:   String
)

object Group {
  def fromFull(fullGroup: FullGroup): Group =
    Group(
      id = fullGroup.id,
      creatorUserId = fullGroup.creatorUserId,
      accessHash = fullGroup.accessHash,
      title = fullGroup.title,
      isPublic = fullGroup.isPublic,
      createdAt = fullGroup.createdAt,
      description = fullGroup.description
    )
}

case class FullGroup(
  id:                   Int,
  creatorUserId:        Int,
  accessHash:           Long,
  title:                String,
  isPublic:             Boolean,
  createdAt:            DateTime,
  description:          String,
  titleChangerUserId:   Int,
  titleChangedAt:       DateTime,
  titleChangeRandomId:  Long,
  avatarChangerUserId:  Int,
  avatarChangedAt:      DateTime,
  avatarChangeRandomId: Long
)
