/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import jsEmoji from 'js-emoji';
import { Path } from 'constants/ActorAppConstants';

export const emoji = jsEmoji;

emoji.include_title = true;
emoji.include_text = true;
emoji.use_sheet = true;
emoji.img_set = 'apple';
emoji.img_sets = {
  'apple': {
    'path': Path.toEmoji + '/img-apple-64/',
    'sheet': Path.toEmoji + '/sheet_apple_64.png',
    'mask': 1
  }
};

export const emojiRegexp = /([\uE000-\uF8FF]|\uD83C[\uDF00-\uDFFF]|\uD83D[\uDC00-\uDDFF])/g;

export default {
  emoji,
  emojiRegexp
};
