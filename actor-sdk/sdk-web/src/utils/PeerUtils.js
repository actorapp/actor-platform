import _ from 'lodash';

import { PeerTypes, PeerTypePrefixes } from '../constants/ActorAppConstants';

import ActorClient from './ActorClient';

export default {
  peerToString(peer) {
    switch (peer.type) {
      case PeerTypes.USER:
        return PeerTypePrefixes.USER + peer.id;
      case PeerTypes.GROUP:
        return PeerTypePrefixes.GROUP + peer.id;
      default:
        throw new Error('Unknown peer type: ' + peer.type + ' ' + peer.id);
    }
  },

  stringToPeer(str) {
    const peerId = parseInt(str.substring(1), 10);
    switch (str.substring(0, 1)) {
      case PeerTypePrefixes.USER:
        return ActorClient.getUserPeer(peerId);
      case PeerTypePrefixes.GROUP:
        return ActorClient.getGroupPeer(peerId);
      default:
        throw new Error('Unknown peer type: ' + str);
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
    return (
      (_.isPlainObject(peer1) && !_.isPlainObject(peer2)) ||
      (!_.isPlainObject(peer1) && _.isPlainObject(peer2)) ||
      (peer1.type === peer2.type && peer1.id === peer2.id)
    );
  }
};
