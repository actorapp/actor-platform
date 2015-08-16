import ActorAppDispatcher from 'dispatcher/ActorAppDispatcher';
import ActorClient from 'utils/ActorClient';
import { ActionTypes } from 'constants/ActorAppConstants';
import ContactActionCreators from 'actions/ContactActionCreators';
import DialogActionCreators from 'actions/DialogActionCreators';
import mixpanel from 'utils/Mixpanel';

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
          const user = users[0];
          const uid = user.id;
          const userPeer = ActorClient.getUserPeer(uid);

          if (user.isContact) {
            DialogActionCreators.selectDialogPeer(userPeer);
            ActorAppDispatcher.dispatch({
              type: ActionTypes.CONTACT_ADD_MODAL_FIND_USER_IN_CONTACT
            });
          } else {
            ContactActionCreators.addContact(uid);
            DialogActionCreators.selectDialogPeer(userPeer);
            mixpanel.track('Add user to contacts by phone number');
            ActorAppDispatcher.dispatch({
              type: ActionTypes.CONTACT_ADD_MODAL_FIND_USER_OK
            });
          }
        } else {
          mixpanel.track('Try to add unregistered user');
          ActorAppDispatcher.dispatch({
            type: ActionTypes.CONTACT_ADD_MODAL_FIND_USER_UNREGISTERED
          });
        }
      }).catch(error => {
        throw new Error(error);
      });
  }
};
