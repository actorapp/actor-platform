/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { dispatch } from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';

const FaviconActionCreators = {
  setFavicon(counter) {
    dispatch(ActionTypes.FAVICON_SET, { counter: counter ? counter.counter : 0 });
  }
};

export default FaviconActionCreators;
