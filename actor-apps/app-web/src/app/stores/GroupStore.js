import ActorClient from '../utils/ActorClient';

const GroupStore = {
  getGroup(gid) {
    return ActorClient.getGroup(gid);
  }
};

export default GroupStore;
