import ActorAppDispatcher from 'dispatcher/ActorAppDispatcher';
import { ActionTypes } from 'constants/ActorAppConstants';

export default {
  openModal: () => {
    ActorAppDispatcher.dispatch({
      type: ActionTypes.APP_UPDATE_MODAL_SHOW
    });
  },

  closeModal: () => {
    ActorAppDispatcher.dispatch({
      type: ActionTypes.APP_UPDATE_MODAL_HIDE
    });
  },

  confirmUpdate: () => {
    window.location.reload();
  }

};
