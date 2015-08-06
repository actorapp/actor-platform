import ActorAppDispatcher from 'dispatcher/ActorAppDispatcher';
import ActorAppConstants from 'constants/ActorAppConstants';

const ActionTypes = ActorAppConstants.ActionTypes;

export default {
  show() {
    ActorAppDispatcher.dispatch({
      type: ActionTypes.SETTINGS_SHOW
    });
  },

  hide() {
    ActorAppDispatcher.dispatch({
      type: ActionTypes.SETTINGS_HIDE
    });
  }
};
