import ActorAppDispatcher from 'dispatcher/ActorAppDispatcher';
import { ActionTypes } from 'constants/ActorAppConstants';

export default {
  show: (group) => {
    ActorAppDispatcher.dispatch({
      type: ActionTypes.INVITE_USER_MODAL_SHOW,
      group: group
    });
  },

  hide: () => {
    ActorAppDispatcher.dispatch({
      type: ActionTypes.INVITE_USER_MODAL_HIDE
    });
  }
};
