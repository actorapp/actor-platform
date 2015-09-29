import { EventEmitter } from 'events';
import ActorAppDispatcher from 'dispatcher/ActorAppDispatcher';
import { ActionTypes, AddContactMessages } from 'constants/ActorAppConstants';

const CHANGE_EVENT = 'change';

let _isOpen = false,
    _message = null;

class AddContactStore extends EventEmitter {
  constructor() {
    super();
  }

  isModalOpen() {
    return _isOpen;
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
      _isOpen = true;
      AddContactStoreInstance.emitChange();
      break;
    case ActionTypes.CONTACT_ADD_MODAL_HIDE:
      _isOpen = false;
      _message = null;
      AddContactStoreInstance.emitChange();
      break;
    case ActionTypes.CONTACT_ADD_MODAL_FIND_USER_OK:
      _isOpen = false;
      _message = null;
      AddContactStoreInstance.emitChange();
      break;
    case ActionTypes.CONTACT_ADD_MODAL_FIND_USER_UNREGISTERED:
      _message = AddContactMessages.PHONE_NOT_REGISTERED;
      AddContactStoreInstance.emitChange();
      break;
    case ActionTypes.CONTACT_ADD_MODAL_FIND_USER_IN_CONTACT:
      _message = AddContactMessages.ALREADY_HAVE;
      AddContactStoreInstance.emitChange();
      break;
    default:
      return;
  }
});

export default AddContactStoreInstance;
