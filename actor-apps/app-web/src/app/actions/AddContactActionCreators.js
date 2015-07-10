import ActorAppDispatcher from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';

export default {
  openModal: () => {
    ActorAppDispatcher.dispatch({
      type: ActionTypes.CONTACT_ADD_MODAL_SHOW
    });
  },

  closeModal: () => {
    ActorAppDispatcher.dispatch({
      type: ActionTypes.CONTACT_ADD_MODAL_HIDE
    });
  },

  findUsers: phone => {
    ActorAppDispatcher.dispatch({
      type: ActionTypes.CONTACT_ADD_MODAL_FIND_USERS,
      phone: phone
    });
  }
};
