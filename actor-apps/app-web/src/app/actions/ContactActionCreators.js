import ActorAppDispatcher from 'dispatcher/ActorAppDispatcher';
import { ActionTypes } from 'constants/ActorAppConstants';
import mixpanel from 'utils/Mixpanel';

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
    mixpanel.track('Add user to contacts');
    ActorAppDispatcher.dispatch({
      type: ActionTypes.CONTACT_ADD,
      uid: uid
    });
  },

  removeContact: (uid) => {
    mixpanel.track('Remove user from contacts');
    ActorAppDispatcher.dispatch({
      type: ActionTypes.CONTACT_REMOVE,
      uid: uid
    });
  }
};
