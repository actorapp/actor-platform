import _ from 'lodash';
import ActorClient from './ActorClient';

export const hasMember = (gid, uid) => {
  const group = ActorClient.getGroup(gid);
  let isMember = false;

  _.forEach(group.members, member => {
    if (member.peerInfo.peer.id === uid) isMember = true;
  });

  return isMember;
};
