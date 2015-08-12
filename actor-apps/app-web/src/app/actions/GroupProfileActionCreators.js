import ActorAppDispatcher from 'dispatcher/ActorAppDispatcher';
import { ActionTypes } from 'constants/ActorAppConstants';
import ActorClient from 'utils/ActorClient';

const GroupProfileActionCreators = {
  getUser(id) {
    console.warn("GroupProfileActionCreators.getUser is deprecated, use ActorClient.getUser or UserStore.getUser");
    return ActorClient.getUser(id);
  },

  getIntegrationToken(gid) {
    ActorClient.getIntegrationToken(gid).then((token) => {
      ActorAppDispatcher.dispatch({
        type: ActionTypes.GET_INTEGRATION_TOKEN,
        token: token
      });
    });
  }
};

export default GroupProfileActionCreators;

