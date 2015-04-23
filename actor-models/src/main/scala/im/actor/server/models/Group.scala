package im.actor.server.models

import org.joda.time.DateTime

case class Group(
  id:            Int,
  creatorUserId: Int,
  accessHash:    Long,
  title:         String,
  createdAt:     DateTime
)

case class FullGroup(
  id:                   Int,
  creatorUserId:        Int,
  accessHash:           Long,
  title:                String,
  createdAt:            DateTime,
  titleChangerUserId:   Int,
  titleChangedAt:       DateTime,
  titleChangeRandomId:  Long,
  avatarChangerUserId:  Int,
  avatarChangedAt:      DateTime,
  avatarChangeRandomId: Long
)
