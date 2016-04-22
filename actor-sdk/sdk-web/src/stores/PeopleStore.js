/*
 * Copyright (C) 2016 Actor LLC. <https://actor.im>
 */

import { ReduceStore } from 'flux/utils';
import Dispatcher from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';
import ActorClient from '../utils/ActorClient';

class PeopleStore extends ReduceStore {
  getInitialState() {
    return [];
  }

  reduce(state, action) {
    if (action.type === ActionTypes.CONTACT_LIST_CHANGED) {
      const uid = ActorClient.getUid();
      return action.contacts.filter((contact) => contact.uid !== uid);
    }

    return state;
  }
}

export default new PeopleStore(Dispatcher);
