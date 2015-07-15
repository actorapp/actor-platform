import ActorAppDispatcher from 'dispatcher/ActorAppDispatcher';
import ActorAppConstants from 'constants/ActorAppConstants';

const ActionTypes = ActorAppConstants.ActionTypes;

export default {
  show() {
    ActorAppDispatcher.dispatch({
      type: ActionTypes.SHOW_ACTIVITY
    });
  },

  hide() {
    ActorAppDispatcher.dispatch({
      type: ActionTypes.HIDE_ACTIVITY
    });
  }
};
