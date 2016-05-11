/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import { dispatch } from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';
import ActorClient from '../utils/ActorClient';

class BlockedUsersActionCreators {
  setQuery(query) {
    dispatch(ActionTypes.BLOCKED_USERS_SET_QUERY, { query });
  }

  loadUsers() {
    dispatch(ActionTypes.BLOCKED_USERS_LOAD);
    ActorClient.loadBlockedUsers().then((users) => {
      dispatch(ActionTypes.BLOCKED_USERS_SET, { users });
    }).catch((error) => {
      dispatch(ActionTypes.BLOCKED_USERS_LOAD_FAILED, { error });
    });
  }

  blockUser(id) {
    ActorClient.blockUser(id).then(() => {
      console.debug('users blocked ' + id);
    }).catch((e) => {
      console.error(e);
    });
  }

  unblockUser(id, reload = false) {
    ActorClient.unblockUser(id).then(() => {
      if (reload) {
        this.loadUsers();
      }
    }).catch((e) => {
      console.error(e);
    });
  }

}

export default new BlockedUsersActionCreators();
