import ActorAppDispatcher from 'dispatcher/ActorAppDispatcher';
import { ActionTypes } from 'constants/ActorAppConstants';
import ActorClient from 'utils/ActorClient';

const GroupProfileActionCreators = {
  getGroup(gid) {
    return ActorClient.getGroup(gid);
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

