/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import ActorClient from '../utils/ActorClient';

const PeerStore = {
  getUserPeer(uid) {
    return ActorClient.getUserPeer(uid);
  },

  getGroupPeer(gid) {
    return ActorClient.getGroupPeer(gid);
  }
};

export default PeerStore;
