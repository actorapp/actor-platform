import { dispatchAsync } from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';
import ActorClient from '../utils/ActorClient';

const GroupProfileActionCreators = {
  getIntegrationToken(gid) {
    dispatchAsync(ActorClient.getIntegrationToken(gid), {
      request: ActionTypes.GROUP_GET_TOKEN,
      success: ActionTypes.GROUP_GET_TOKEN_SUCCESS,
      failure: ActionTypes.GROUP_GET_TOKEN_ERROR
    }, { gid });
  }
};

export default GroupProfileActionCreators;

