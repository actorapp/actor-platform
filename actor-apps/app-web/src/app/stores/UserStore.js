import ActorClient from 'utils/ActorClient';

const UserStore = {
  getUser(uid) {
    return ActorClient.getUser(uid);
  },

  getMyId() {
    return ActorClient.getUid();
  }
};

export default UserStore;
