import ActorClient from '../utils/ActorClient';

const GroupStore = {
  getGroup(gid) {
    return ActorClient.getGroup(gid);
  },

  getIntegrationToken(gid) {
    return ActorClient.getIntegrationToken(gid);
  }
};

export default GroupStore;
