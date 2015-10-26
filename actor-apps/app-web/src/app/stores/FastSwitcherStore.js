/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { forEach, assign } from 'lodash';

import { Store } from 'flux/utils';
import Dispatcher from 'dispatcher/ActorAppDispatcher';
import { ActionTypes } from 'constants/ActorAppConstants';

import DialogStore from 'stores/DialogStore';
import ContactStore from 'stores/ContactStore';

let _isOpen = false,
    _result = [];

class FastSwitcherStore extends Store {
  isOpen() {
    return _isOpen;
  }

  getResults() {
    return _result;
  }

  handleSearchQuery(query) {
    const dialogs = DialogStore.getAll();
    const contacts = ContactStore.getContacts();

    let result = [];

    forEach(dialogs, (dialog) => {
      if (dialog.peer.title.toLocaleLowerCase().includes(query.toLocaleLowerCase())) {
        result.push({type: 'DIALOG', dialog});
      }
    });

    forEach(contacts, (contact) => {
      if (contact.name.toLocaleLowerCase().includes(query.toLocaleLowerCase())) {
        result.push({type: 'CONTACT', contact});
      }
    });

    if (result.length === 0) {
      result.push({type: 'SUGGESTION', query});
    }

    console.debug('result', result);

    _result = result;
    this.__emitChange();
  }

  __onDispatch = (action) => {
    switch (action.type) {
      case ActionTypes.FAST_SWITCHER_SHOW:
        _isOpen = true;
        this.handleSearchQuery('');
        this.__emitChange();
        break;

      case ActionTypes.FAST_SWITCHER_HIDE:
        _isOpen = false;
        _result = [];
        this.__emitChange();
        break;

      case ActionTypes.FAST_SWITCHER_SEARCH:
        this.handleSearchQuery(action.query);
        break;
    }
  }
}

export default new FastSwitcherStore(Dispatcher);
