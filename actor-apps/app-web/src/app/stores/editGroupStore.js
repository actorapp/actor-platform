import { EventEmitter } from 'events';
import { register } from 'dispatcher/ActorAppDispatcher';
import { ActionTypes } from 'constants/ActorAppConstants';
import ActorClient from 'utils/ActorClient';

const CHANGE_EVENT = 'change';

let _isOpen = false,
    _title = '',
    _group = {};

class EditGroupStore extends EventEmitter {
  emitChange() {
    this.emit(CHANGE_EVENT);
  }

  addChangeListener(callback) {
    this.on(CHANGE_EVENT, callback);
  }

  removeChangeListener(callback) {
    this.removeListener(CHANGE_EVENT, callback);
  }

  isOpen() {
    return _isOpen;
  }

  getGroup() {
    return _group;
  }

  getTitle() {
    return _title;
  }
}

let EditGroupStoreInstance = new EditGroupStore();

EditGroupStoreInstance.dispatchToken = register(action => {
  switch (action.type) {
    case ActionTypes.GROUP_EDIT_MODAL_SHOW:
      _group = ActorClient.getGroup(action.gid);
      _isOpen = true;
      _title = _group.name;
      EditGroupStoreInstance.emitChange();
      break;

    case ActionTypes.GROUP_EDIT_MODAL_HIDE:
      _isOpen = false;
      EditGroupStoreInstance.emitChange();
      break;
  }
});

export default EditGroupStoreInstance;
