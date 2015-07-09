import { EventEmitter } from 'events';
import ActorAppDispatcher from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';

const CHANGE_EVENT = 'change';

let _isModalOpen = false;

class AddContactStore extends EventEmitter {
  constructor() {
    super();
  }

  isModalOpen() {
    return _isModalOpen;
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
      break;
    case ActionTypes.CONTACT_ADD_MODAL_HIDE:
      _isModalOpen = false;
      break;
    default:
      return;
  }
  AddContactStoreInstance.emitChange();
});

export default AddContactStoreInstance;
