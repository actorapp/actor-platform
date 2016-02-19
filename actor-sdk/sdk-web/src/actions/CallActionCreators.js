/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import { dispatch, dispatchAsync } from '../dispatcher/ActorAppDispatcher';
import { ActionTypes, CallTypes } from '../constants/ActorAppConstants';
import ActorClient from '../utils/ActorClient';
import CallStore from '../stores/CallStore';

const HIDE_MODAL_AFTER = 3000;

export default {
  hide() {
    dispatch(ActionTypes.CALL_MODAL_HIDE);
  },

  handleCall(event) {
    const { id, type } = event;
    switch (type) {
      case CallTypes.STARTED:
        ActorClient.bindCall(id, this.setCall);
        dispatch(ActionTypes.CALL_MODAL_OPEN, { id });
        break;
      case CallTypes.ENDED:
        setTimeout(() => {
          ActorClient.unbindCall(id, this.setCall);
          if (CallStore.isOpen()) dispatch(ActionTypes.CALL_MODAL_HIDE);
        }, HIDE_MODAL_AFTER);
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
