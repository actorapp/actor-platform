import ActorAppDispatcher from 'dispatcher/ActorAppDispatcher';
import { ActionTypes } from 'constants/ActorAppConstants';

export default {
  showContactList: () => {
    ActorAppDispatcher.dispatch({
      type: ActionTypes.CONTACT_LIST_SHOW
    });
  },

  hideContactList: () => {
    ActorAppDispatcher.dispatch({
      type: ActionTypes.CONTACT_LIST_HIDE
    });
  },

  setContacts: (contacts) => {
    ActorAppDispatcher.dispatch({
      type: ActionTypes.CONTACT_LIST_CHANGED,
      contacts: contacts
    });
  },

  addContact: (uid) => {
    ActorAppDispatcher.dispatch({
      type: ActionTypes.CONTACT_ADD,
      uid: uid
    });
  },

  removeContact: (uid) => {
    ActorAppDispatcher.dispatch({
      type: ActionTypes.CONTACT_REMOVE,
      uid: uid
    });
  }
};
