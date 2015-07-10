import ActorAppDispatcher from '../dispatcher/ActorAppDispatcher';
import ActorClient from '../utils/ActorClient';
import { ActionTypes } from '../constants/ActorAppConstants';
import ContactActionCreators from './ContactActionCreators';
import DialogActionCreators from './DialogActionCreators';

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
    ActorClient.findUsers(phone)
      .then(users => {
        if (users.length > 0) {
          const uid = users[0].id;
          const userPeer = ActorClient.getUserPeer(uid);

          ContactActionCreators.addContact(uid);
          DialogActionCreators.selectDialogPeer(userPeer);

          ActorAppDispatcher.dispatch({
            type: ActionTypes.CONTACT_ADD_MODAL_FIND_USER_OK
          });

        } else {
          ActorAppDispatcher.dispatch({
            type: ActionTypes.CONTACT_ADD_MODAL_FIND_USER_UNREGISTERED
          });
        }
      }).catch(error => {
        throw new Error(error);
      });
  }
};
