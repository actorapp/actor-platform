/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { ReduceStore } from 'flux/utils';
import { Map } from 'immutable';
import Dispatcher from '../dispatcher/ActorAppDispatcher';
import { ActionTypes, AsyncActionStates } from '../constants/ActorAppConstants';

class DialogSearchStore extends ReduceStore {
  getInitialState() {
    return {
      isOpen: false,
      query: '',
      filter: new Map({
        text: true,
        docs: true,
        links: true,
        photos: true
      }),
      result: new Map({
        text: [],
        docs: [],
        links: [],
        photos: []
      }),
      status: new Map({
        text: AsyncActionStates.SUCCESS,
        docs: AsyncActionStates.SUCCESS,
        links: AsyncActionStates.SUCCESS,
        photos: AsyncActionStates.SUCCESS
      })
    };
  }

  reduce(state, action) {
    switch (action.type) {
      case ActionTypes.DIALOG_SEARCH_SHOW:
        return {
          ...state,
          isOpen: true
        };

      case ActionTypes.BIND_DIALOG_PEER:
      case ActionTypes.DIALOG_SEARCH_HIDE:
        return this.getInitialState();

      case ActionTypes.DIALOG_SEARCH_CHANGE_QUERY:
        return {
          ...state,
          query: action.query
        }

      case ActionTypes.DIALOG_SEARCH_TEXT_SUCCESS:
        return {
          ...state,
          result: state.result.set('text', action.response),
          status: state.result.set('text', AsyncActionStates.SUCCESS)
        };

      case ActionTypes.DIALOG_SEARCH_DOCS_SUCCESS:
        return {
          ...state,
          result: state.result.set('docs', action.response)
        };

      case ActionTypes.DIALOG_SEARCH_LINKS_SUCCESS:
        return {
          ...state,
          result: state.result.set('links', action.response)
        };

      case ActionTypes.DIALOG_SEARCH_PHOTO_SUCCESS:
        return {
          ...state,
          result: state.result.set('photos', action.response)
        };

      default:
        return state;
    }
  }
}

export default new DialogSearchStore(Dispatcher);
