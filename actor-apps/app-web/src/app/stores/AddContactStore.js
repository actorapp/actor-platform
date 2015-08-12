import { EventEmitter } from 'events';
import ActorAppDispatcher from 'dispatcher/ActorAppDispatcher';
import { ActionTypes } from 'constants/ActorAppConstants';

const CHANGE_EVENT = 'change';

let _isModalOpen = false,
    _message = null;

class AddContactStore extends EventEmitter {
  constructor() {
    super();
  }

  isModalOpen() {
    return _isModalOpen;
  }

  getMessage() {
    return _message;
  }

  emitChange() {
    this.emit(CHANGE_EVENT);
  }

  addChangeListener(callback) {
    this.on(CHANGE_EVENT, callback);
  }

  removeChangeListener(callback) {
    this.removeListener(CHANGE_EVENT, callback);
  }
}

let AddContactStoreInstance = new AddContactStore();

AddContactStoreInstance.dispatchToken = ActorAppDispatcher.register(action => {
  switch(action.type) {
    case ActionTypes.CONTACT_ADD_MODAL_SHOW:
      _isModalOpen = true;
      AddContactStoreInstance.emitChange();
      break;
    case ActionTypes.CONTACT_ADD_MODAL_HIDE:
      _isModalOpen = false;
      _message = null;
      AddContactStoreInstance.emitChange();
      break;
    case ActionTypes.CONTACT_ADD_MODAL_FIND_USER_OK:
      _isModalOpen = false;
      _message = null;
      AddContactStoreInstance.emitChange();
      break;
    case ActionTypes.CONTACT_ADD_MODAL_FIND_USER_UNREGISTERED:
      _message = 'This phone is not registered in Actor.';
      AddContactStoreInstance.emitChange();
      break;
    case ActionTypes.CONTACT_ADD_MODAL_FIND_USER_IN_CONTACT:
      _message = 'You already have this user in contacts.';
      AddContactStoreInstance.emitChange();
      break;
    default:
      return;
  }
});

export default AddContactStoreInstance;
