import _ from 'lodash';

import { PeerTypes } from 'constants/ActorAppConstants';

import ActorClient from 'utils/ActorClient';

export default {
  peerToString: (peer) => {
    let str;

    switch (peer.type) {
      case PeerTypes.USER:
            str = 'u' + peer.id;
            break;
      case PeerTypes.GROUP:
            str = 'g' + peer.id;
            break;
      default:
            throw('Unknown peer type' + peer.type + ' ' + peer.id);
    }

    return str;
  },

  stringToPeer: (str) => {
    let peer = null;

    if (str) {
      const peerId = parseInt(str.substring(1));

      if (peerId > 0) {
        switch (str.substring(0, 1)) {
          case 'u':
            peer = ActorClient.getUserPeer(peerId);
            break;
          case 'g':
            peer = ActorClient.getGroupPeer(peerId);
            break;
          default:
        }
      }
    }

    return peer;
  },

  equals: (peer1, peer2) => {
    return (
      (_.isPlainObject(peer1) && !_.isPlainObject(peer2)) ||
      (!_.isPlainObject(peer1) && _.isPlainObject(peer2)) ||
      (peer1.type === peer2.type && peer1.id === peer2.id)
    );
  }
};
