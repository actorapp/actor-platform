import ActorAppDispatcher from 'dispatcher/ActorAppDispatcher';
import { ActionTypes } from 'constants/ActorAppConstants';

export default {
  show() {
    ActorAppDispatcher.dispatch({
      type: ActionTypes.MY_PROFILE_MODAL_SHOW
    });
  },

  hide() {
    ActorAppDispatcher.dispatch({
      type: ActionTypes.MY_PROFILE_MODAL_HIDE
    });
  },

  saveName(name) {
    ActorAppDispatcher.dispatch({
      type: ActionTypes.MY_PROFILE_SAVE_NAME,
      name: name
    });
  },

  saveNickname(nick) {
    ActorAppDispatcher.dispatch({
      type: ActionTypes.MY_PROFILE_SAVE_NICKNAME,
      nick: nick
    });
  }
};
