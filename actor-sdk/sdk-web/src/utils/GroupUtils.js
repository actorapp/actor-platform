/*
 * Copyright (C) 2016 Actor LLC. <https://actor.im>
 */

import { find } from 'lodash';

import ActorClient from './ActorClient';

export const hasMember = (gid, uid) => {
  const group = ActorClient.getGroup(gid);

  return undefined !== find(group.members, (c) => c.peerInfo.peer.id === uid);
};
