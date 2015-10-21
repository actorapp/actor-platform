/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { forEach } from 'lodash';

import { Store } from 'flux/utils';
import Dispatcher from 'dispatcher/ActorAppDispatcher';
import { ActionTypes } from 'constants/ActorAppConstants';

import DialogStore from 'stores/DialogStore';

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
    let result = [];
    forEach(dialogs, (dialog) => {
      forEach(dialog.shorts, (conversation) => {
        if (conversation.peer.title.toLocaleLowerCase().includes(query.toLocaleLowerCase())) {
          result.push(conversation);
        }
      })
    });
    _result = result;
    this.__emitChange();
  }

  __onDispatch = (action) => {
    switch (action.type) {
      case ActionTypes.FAST_SWITCHER_SHOW:
        _isOpen = true;
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
