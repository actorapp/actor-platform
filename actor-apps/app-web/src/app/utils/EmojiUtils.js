/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { keys } from 'lodash';
import { Path } from 'constants/ActorAppConstants';
import emojiCharacters from 'emoji-named-characters';

export default {
  names: keys(emojiCharacters),
  pathToImage: (name) => `${Path.toEmoji}/${name}.png`
}
