/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

// import { dispatch, dispatchAsync } from '../dispatcher/ActorAppDispatcher';
// import { ActionTypes } from '../constants/ActorAppConstants';
import ActorClient from '../utils/ActorClient';

class BlockedUsersActionCreators {
  setUsers(users) {
    console.debug('set blocked users', users);
  }

  blockUser(id) {
    ActorClient.blockUser(id).then(() => {
      console.debug('users blocked ' + id);
    }).catch((e) => {
      console.error(e);
    });
  }

  unblockUser(id) {
    ActorClient.unblockUser(id).then(() => {
      console.debug('users unblocked ' + id);
    }).catch((e) => {
      console.error(e);
    });
  }

}

export default new BlockedUsersActionCreators();
