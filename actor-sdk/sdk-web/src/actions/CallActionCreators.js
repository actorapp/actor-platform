/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import { dispatch, dispatchAsync } from '../dispatcher/ActorAppDispatcher';
import { ActionTypes, CallTypes } from '../constants/ActorAppConstants';
import ActorClient from '../utils/ActorClient';

export default {
  handleCall(event) {
    const { id, type } = event;
    switch (type) {
      case CallTypes.STARTED:
        dispatch(ActionTypes.CALL_MODAL_OPEN, { id });
        ActorClient.bindCall(id, this.setCall);
        break;
      case CallTypes.ENDED:
        dispatch(ActionTypes.CALL_MODAL_HIDE, { id });
        ActorClient.unbindCall(id, this.setCall);
        break;
      default:
    }
  },

  makeCall(uid) {
    dispatchAsync(ActorClient.makeCall(uid), {
      request: ActionTypes.CALL,
      success: ActionTypes.CALL_SUCCESS,
      failure: ActionTypes.CALL_ERROR
    }, { uid });
  },

  setCall(call) {
    dispatch(ActionTypes.CALL_CHANGED, { call });
  },

  answerCall(id) {
    ActorClient.answerCall(id);
    dispatch(ActionTypes.CALL_ANSWER, { id })
  },

  endCall(id) {
    ActorClient.endCall(id);
    dispatch(ActionTypes.CALL_END, { id })
  }
}
