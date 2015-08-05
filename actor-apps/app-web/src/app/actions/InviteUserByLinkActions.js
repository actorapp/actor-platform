import ActorAppDispatcher from 'dispatcher/ActorAppDispatcher';
import { ActionTypes } from 'constants/ActorAppConstants';

export default {
  show: (group) => {
    ActorAppDispatcher.dispatch({
      type: ActionTypes.INVITE_USER_BY_LINK_MODAL_SHOW,
      group: group
    });
  },

  hide: () => {
    ActorAppDispatcher.dispatch({
      type: ActionTypes.INVITE_USER_BY_LINK_MODAL_HIDE
    });
  }
};
