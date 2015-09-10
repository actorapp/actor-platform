/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { dispatch } from 'dispatcher/ActorAppDispatcher';
import { ActionTypes } from 'constants/ActorAppConstants';

export default {
  show: () => dispatch(ActionTypes.PROFILE_PICTURE_MODAL_SHOW),
  hide: () => dispatch(ActionTypes.PROFILE_PICTURE_MODAL_HIDE)
}
