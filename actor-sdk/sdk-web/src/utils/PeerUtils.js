import { PeerTypes, PeerTypePrefixes } from '../constants/ActorAppConstants';

import ActorClient from './ActorClient';

export default {
  peerToString(peer) {
    const { id, type } = peer;

    switch (type) {
      case PeerTypes.USER:
        return PeerTypePrefixes.USER + id;
      case PeerTypes.GROUP:
        return PeerTypePrefixes.GROUP + id;
      default:
        console.error('Unknown peer type: { type: %s, id: %s }', type, id);
    }
  },

  stringToPeer(str) {
    const type = str.charAt(0);
    const id = parseInt(str.substring(1), 10);

    switch (type) {
      case PeerTypePrefixes.USER:
        return ActorClient.getUserPeer(id);
      case PeerTypePrefixes.GROUP:
        return ActorClient.getGroupPeer(id);
      default:
      console.error('Unknown peer type: { type: %s, id: %s }', type, id);
    }
  },

  hasPeer(peer) {
    try {
      switch (peer.type) {
        case PeerTypes.USER:
          return ActorClient.getUser(peer.id);
        case PeerTypes.GROUP:
          return ActorClient.getGroup(peer.id);
      }
    } catch (e) {
      console.error(e);
    }

    return false;
  },

  equals(peer1, peer2) {
    return Boolean(peer1 && peer2) && peer1.id === peer2.id && peer1.type === peer2.type;
  },

  isGroupBot(user) {
    return !user.avatar && !user.userName && user.title === 'Bot';
  }
};
