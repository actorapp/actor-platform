/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { Store } from 'flux/utils';
import Dispatcher from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';

class DraftStore extends Store {
  constructor(dispatcher) {
    super(dispatcher);

    this._draft = null;
  }

  getDraft() {
    return this._draft;
  }

  __onDispatch(action) {
    switch (action.type) {
      case ActionTypes.DRAFT_LOAD:
        this._draft = action.draft;
        this.__emitChange();
        break;

      case ActionTypes.DRAFT_CHANGE:
        this._draft = action.draft;
        this.__emitChange();
        break;

      default:
    }
  }
}

export default new DraftStore(Dispatcher);
