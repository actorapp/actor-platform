import ActorAppDispatcher from '../dispatcher/ActorAppDispatcher';
import ActorAppConstants from '../constants/ActorAppConstants';
var ActionTypes = ActorAppConstants.ActionTypes;

export default {
  show: function() {
    ActorAppDispatcher.dispatch({
      type: ActionTypes.SHOW_ACTIVITY
    });
  },

  hide: function() {
    ActorAppDispatcher.dispatch({
      type: ActionTypes.HIDE_ACTIVITY
    });
  }
};
