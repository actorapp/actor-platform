import ActorAppDispatcher from 'dispatcher/ActorAppDispatcher';
import ActorAppConstants from 'constants/ActorAppConstants';

const ActionTypes = ActorAppConstants.ActionTypes;

export default {
  show: () => {
    ActorAppDispatcher.dispatch({
      type: ActionTypes.PREFERENCES_MODAL_SHOW
    });
  },

  hide: () => {
    ActorAppDispatcher.dispatch({
      type: ActionTypes.PREFERENCES_MODAL_HIDE
    });
  },

  //load: () => {
  //  ActorAppDispatcher.dispatch({
  //    type: ActionTypes.PREFERENCES_LOAD
  //  });
  //},

  save: (preferences) => {
    ActorAppDispatcher.dispatch({
      type: ActionTypes.PREFERENCES_SAVE,
      preferences: preferences
    });
  }
};
