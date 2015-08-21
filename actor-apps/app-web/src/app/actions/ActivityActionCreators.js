import ActorAppDispatcher from 'dispatcher/ActorAppDispatcher';
import { ActionTypes } from 'constants/ActorAppConstants';

export default {
  show() {
    ActorAppDispatcher.dispatch({
      type: ActionTypes.ACTIVITY_SHOW
    });
  },

  hide() {
    ActorAppDispatcher.dispatch({
      type: ActionTypes.ACTIVITY_HIDE
    });
  }
};
