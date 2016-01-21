import { dispatch } from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';

export default {
  openModal() {
    dispatch(ActionTypes.APP_UPDATE_MODAL_SHOW);
  },

  closeModal() {
    dispatch(ActionTypes.APP_UPDATE_MODAL_HIDE);
  },

  confirmUpdate() {
    window.location.reload();
  }
};
