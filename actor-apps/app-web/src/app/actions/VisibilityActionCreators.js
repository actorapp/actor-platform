import ActorClient from 'utils/ActorClient';

import ActorAppDispatcher from 'dispatcher/ActorAppDispatcher';
import ActorAppConstants from 'constants/ActorAppConstants';

var ActionTypes = ActorAppConstants.ActionTypes;

export default {
  createAppVisible: function() {
    ActorClient.onAppVisible();

    ActorAppDispatcher.dispatch({
      type: ActionTypes.APP_VISIBLE
    });
  },

  createAppHidden: function() {
    ActorClient.onAppHidden();

    ActorAppDispatcher.dispatch({
      type: ActionTypes.APP_HIDDEN
    });
  }
};
