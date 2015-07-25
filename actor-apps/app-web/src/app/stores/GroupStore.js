import { EventEmitter } from 'events';
import ActorAppDispatcher from 'dispatcher/ActorAppDispatcher';
import { ActionTypes } from 'constants/ActorAppConstants';
import GroupProfileActionCreators from 'actions/GroupProfileActionCreators';

const CHANGE_EVENT = 'change';

let _integrationToken;

class GroupStore extends EventEmitter {
  getGroup(gid) {
    return GroupProfileActionCreators.getGroup(gid);
  }

  getIntegrationToken() {
    return _integrationToken;
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

let GroupStoreInstance = new GroupStore();

GroupStoreInstance.dispatchToken = ActorAppDispatcher.register(action => {
  switch (action.type) {
    case ActionTypes.GET_INTEGRATION_TOKEN:
      _integrationToken = action.token;
      break;
    default:
      return;
  }
  GroupStoreInstance.emitChange();
});

export default GroupStoreInstance;
