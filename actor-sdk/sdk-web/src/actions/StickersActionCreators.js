/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { dispatch } from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';
import ActorClient from '../utils/ActorClient';

class StickersActionCreators {
  setStickers(stickers) {
    dispatch(ActionTypes.STICKERS_SET, { stickers });
  }

  sendSticker(peer, sticker) {
    ActorClient.sendSticker(peer, sticker);
  }
}

export default new StickersActionCreators();
