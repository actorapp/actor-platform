import { dispatchAsync } from 'dispatcher/ActorAppDispatcher';
import { ActionTypes } from 'constants/ActorAppConstants';
import ActorClient from 'utils/ActorClient';

const GroupProfileActionCreators = {
  getUser(uid) {
    console.warn('GroupProfileActionCreators.getUser is deprecated, use ActorClient.getUser or UserStore.getUser');
    return ActorClient.getUser(uid);
  },

  getIntegrationToken(gid) {
    dispatchAsync(ActorClient.getIntegrationToken(gid), {
      request: ActionTypes.GET_INTEGRATION_TOKEN,
      success: ActionTypes.GET_INTEGRATION_TOKEN_SUCCESS,
      failure: ActionTypes.GET_INTEGRATION_TOKEN_ERROR
    }, { gid });
  },


  kickMember(gid, uid) {
    dispatchAsync(ActorClient.kickMember(gid, uid), {
      request: ActionTypes.KICK_USER,
      success: ActionTypes.KICK_USER_SUCCESS,
      failure: ActionTypes.KICK_USER_ERROR
    }, { gid, uid });
  }
};

export default GroupProfileActionCreators;

