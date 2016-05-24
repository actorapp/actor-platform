/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */
import { debounce } from 'lodash';
import { dispatch, dispatchAsync } from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';
import ActorClient from '../utils/ActorClient';

import ComposeActionCreators from './ComposeActionCreators';

import DialogStore from '../stores/DialogStore';

class DialogSearchActionCreators {
  constructor() {
    this.findAllText = debounce(this.findAllText, 300, { trailing: true });
    this.findAllDocs = debounce(this.findAllDocs, 300, { trailing: true });
    this.findAllLinks = debounce(this.findAllLinks, 300, { trailing: true });
    this.findAllPhotos = debounce(this.findAllPhotos, 300, { trailing: true });
  }

  open(query = '') {
    dispatch(ActionTypes.DIALOG_SEARCH_SHOW);
    ComposeActionCreators.toggleAutoFocus(false);

    this.changeSearchQuery(query)
  }

  close() {
    dispatch(ActionTypes.DIALOG_SEARCH_HIDE);
    ComposeActionCreators.toggleAutoFocus(true);
  }

  changeSearchQuery(query, filter = {}) {
    dispatch(ActionTypes.DIALOG_SEARCH_CHANGE_QUERY, { query });

    if (filter.text) this.findAllText(query);
    if (filter.docs) this.findAllDocs();
    if (filter.links) this.findAllLinks();
    if (filter.photos) this.findAllPhotos();
  }

  findAllText(query) {
    const peer = DialogStore.getCurrentPeer();
    dispatchAsync(ActorClient.findAllText(peer, query), {
      request: ActionTypes.DIALOG_SEARCH_TEXT,
      success: ActionTypes.DIALOG_SEARCH_TEXT_SUCCESS,
      failure: ActionTypes.DIALOG_SEARCH_TEXT_ERROR
    }, { peer, query });
  }

  findAllDocs() {
    const peer = DialogStore.getCurrentPeer();
    dispatchAsync(ActorClient.findAllDocs(peer), {
      request: ActionTypes.DIALOG_SEARCH_DOCS,
      success: ActionTypes.DIALOG_SEARCH_DOCS_SUCCESS,
      failure: ActionTypes.DIALOG_SEARCH_DOCS_ERROR
    }, { peer });
  }

  findAllLinks() {
    const peer = DialogStore.getCurrentPeer();
    dispatchAsync(ActorClient.findAllLinks(peer), {
      request: ActionTypes.DIALOG_SEARCH_LINKS,
      success: ActionTypes.DIALOG_SEARCH_LINKS_SUCCESS,
      failure: ActionTypes.DIALOG_SEARCH_LINKS_ERROR
    }, { peer });
  }

  findAllPhotos() {
    const peer = DialogStore.getCurrentPeer();
    dispatchAsync(ActorClient.findAllPhotos(peer), {
      request: ActionTypes.DIALOG_SEARCH_PHOTO,
      success: ActionTypes.DIALOG_SEARCH_PHOTO_SUCCESS,
      failure: ActionTypes.DIALOG_SEARCH_PHOTO_ERROR
    }, { peer });
  }
}

export default new DialogSearchActionCreators();
