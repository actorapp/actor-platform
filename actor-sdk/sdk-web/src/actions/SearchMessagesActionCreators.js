/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { debounce } from 'lodash';
import { dispatch, dispatchAsync } from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';
import ActorClient from '../utils/ActorClient';
import DialogStore from '../stores/DialogStore';
import SearchMessagesStore from '../stores/SearchMessagesStore';
import ComposeActionCreators from './ComposeActionCreators';

class SearchMessagesActionCreators {
  constructor() {
    this.findText = debounce(this.findText.bind(this), 100, { maxWait: 300 });
  }

  open() {
    dispatch(ActionTypes.SEARCH_MESSAGES_SHOW);
    ComposeActionCreators.toggleAutoFocus(false);
  }

  close() {
    dispatch(ActionTypes.SEARCH_MESSAGES_HIDE);
    ComposeActionCreators.toggleAutoFocus(true);
  }

  setQuery(query) {
    dispatch(ActionTypes.SEARCH_MESSAGES_SET_QUERY, { query });
    this.findText();
  }

  findText() {
    const { query } = SearchMessagesStore.getState();
    if (!query) {
      return;
    }

    const peer = DialogStore.getCurrentPeer();
    dispatchAsync(ActorClient.findAllText(peer, query), {
      request: ActionTypes.SEARCH_TEXT,
      success: ActionTypes.SEARCH_TEXT_SUCCESS,
      failure: ActionTypes.SEARCH_TEXT_ERROR
    }, { peer, query });
  }
}

export default new SearchMessagesActionCreators();
