/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { dispatch, dispatchAsync } from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';
import ActorClient from '../utils/ActorClient';

import ActivityActionCreators from './ActivityActionCreators';

import DialogStore from '../stores/DialogStore';
import ActivityStore from '../stores/ActivityStore';
import SearchMessagesStore from '../stores/SearchMessagesStore';

class SearchMessagesActionCreators {
  constructor() {
    this.isActivityOpenBeforeSearch = false;
  }

  toggleOpen(isOpen) {
    if (isOpen) {
      dispatch(ActionTypes.SEARCH_SHOW);
      // TODO: move this to store
      this.isActivityOpenBeforeSearch = ActivityStore.isOpen();
      if (this.isActivityOpenBeforeSearch) {
        ActivityActionCreators.hide()
      }
    } else {
      dispatch(ActionTypes.SEARCH_HIDE);
      if (this.isActivityOpenBeforeSearch) {
        ActivityActionCreators.show();
      }
    }
  }

  toggleFocus(isEnable) {
    dispatch(ActionTypes.SEARCH_TOGGLE_FOCUS, { isEnable });
  }

  toggleExpand() {
    dispatch(ActionTypes.SEARCH_TOGGLE_EXPAND);
  }

  setQuery(query) {
    dispatch(ActionTypes.SEARCH_TEXT, { query });
  }

  findAllText(query) {
    if (!query) {
      return;
    }

    const isSearchOpen = SearchMessagesStore.isOpen();
    const peer = DialogStore.getCurrentPeer();

    if (!isSearchOpen) {
      this.toggleOpen(true);
    }

    dispatchAsync(ActorClient.findAllText(peer, query), {
      request: ActionTypes.SEARCH_TEXT,
      success: ActionTypes.SEARCH_TEXT_SUCCESS,
      failure: ActionTypes.SEARCH_TEXT_ERROR
    }, { peer, query });
  }

  findAllDocs() {
    const peer = DialogStore.getCurrentPeer();
    dispatchAsync(ActorClient.findAllDocs(peer), {
      request: ActionTypes.SEARCH_DOCS,
      success: ActionTypes.SEARCH_DOCS_SUCCESS,
      failure: ActionTypes.SEARCH_DOCS_ERROR
    }, { peer });
  }

  findAllLinks() {
    const peer = DialogStore.getCurrentPeer();
    dispatchAsync(ActorClient.findAllLinks(peer), {
      request: ActionTypes.SEARCH_LINKS,
      success: ActionTypes.SEARCH_LINKS_SUCCESS,
      failure: ActionTypes.SEARCH_LINKS_ERROR
    }, { peer });
  }

  findAllPhotos() {
    const peer = DialogStore.getCurrentPeer();
    dispatchAsync(ActorClient.findAllPhotos(peer), {
      request: ActionTypes.SEARCH_PHOTO,
      success: ActionTypes.SEARCH_PHOTO_SUCCESS,
      failure: ActionTypes.SEARCH_PHOTO_ERROR
    }, { peer });
  }
}

export default new SearchMessagesActionCreators();
