/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { ReduceStore } from 'flux/utils';
import Dispatcher from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';

class DialogSearchStore extends ReduceStore {
  getInitialState() {
    return {
      isOpen: false,
      query: '',
      filter: {
        searchText: true,
        searchDocs: true,
        searchLinks: true,
        searchPhotos: true
      },
      results: []
    };
  }

  reduce(state, action) {
    switch (action.type) {
      case ActionTypes.DIALOG_SEARCH_SHOW:
        return {
          ...state,
          isOpen: true
        };
      case ActionTypes.DIALOG_SEARCH_CHANGE_QUERY:
        return {
          ...state,
          query: action.query
        }

      case ActionTypes.DIALOG_SEARCH_TEXT_SUCCESS:
        return {
          ...state,
          results: action.response
        }

      case ActionTypes.DIALOG_SEARCH_DOCS_SUCCESS:
      case ActionTypes.DIALOG_SEARCH_LINKS_SUCCESS:
      case ActionTypes.DIALOG_SEARCH_PHOTO_SUCCESS:
        // TODO: correctly add response to results
        return state;

      case ActionTypes.DIALOG_SEARCH_HIDE:
        return this.getInitialState();

      default:
        return state;
    }
  }
}

export default new DialogSearchStore(Dispatcher);
