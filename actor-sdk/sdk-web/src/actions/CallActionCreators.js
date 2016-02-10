/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import { dispatch, dispatchAsync } from '../dispatcher/ActorAppDispatcher';
import { ActionTypes, CallStates } from '../constants/ActorAppConstants';
import ActorClient from '../utils/ActorClient';

export default {
  handleCall(event) {
    ActorClient.bindCall(event.id, this.setCall);
    dispatch(ActionTypes.CALL_MODAL_OPEN, { event })
  },

  makeCall(uid) {
    dispatchAsync(ActorClient.makeCall(uid), {
      request: ActionTypes.CALL,
      success: ActionTypes.CALL_SUCCESS,
      failure: ActionTypes.CALL_ERROR
    }, { uid });
  },

  setCall(call) {
    console.debug('setCall', call);
    dispatch(ActionTypes.CALL_CHANGED, { call });

    switch (call.state) {
      case CallStates.ENDED:
        dispatch(ActionTypes.CALL_MODAL_HIDE);
        break;
      default:
    }
  }
}
