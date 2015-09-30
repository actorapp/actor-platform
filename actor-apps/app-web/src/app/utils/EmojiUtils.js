/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { escape } from 'lodash';
import emoji from 'js-emoji';
import emojiDataCategories from 'emoji-data/build/emoji_categories';
import { Path } from 'constants/ActorAppConstants';

emoji.include_title = true;
emoji.include_text = true;
emoji.use_sheet = true;
emoji.colons_mode = false;
emoji.img_set = 'apple';
emoji.img_sets = {
  'apple': {
    'path': Path.toEmoji + '/img-apple-64/',
    'sheet': Path.toEmoji + '/sheet_apple_64.png',
    'mask': 1
  },
  'google': {
    'path': Path.toEmoji + '/img-google-64/',
    'sheet': Path.toEmoji + '/sheet_google_64.png',
    'mask': 2
  },
  'twitter': {
    'path': Path.toEmoji + '/img-twitter-64/',
    'sheet': Path.toEmoji + '/sheet_twitter_64.png',
    'mask': 4
  },
  'emojione': {
    'path': Path.toEmoji + '/img-emojione-64/',
    'sheet': Path.toEmoji + '/sheet_emojione_64.png',
    'mask': 8
  }
};

export { emoji as emoji };

export const getEmojiCategories = () => {
  let emojiCategories = [];

  for (let category of emojiDataCategories.EmojiDataArray) {
    let title = category.CVDataTitle.replace(/^(.*)-/, '');
    let data = category.CVCategoryData.Data.split(',');
    let icon = '';

    switch (title) {
      case 'People':
        icon = ':grinning:';
        break;
      case 'Nature':
        icon = ':evergreen_tree:';
        break;
      case 'Foods':
        icon = ':hamburger:';
        break;
      case 'Celebration':
        icon = ':gift:';
        break;
      case 'Activity':
        icon = ':football:';
        break;
      case 'Places':
        icon = ':airplane:';
        break;
      case 'Flags':
        icon = ':flag-ru:';
        break;
      case 'Symbols':
        icon = ':eyeglasses:';
        break;
      default:
    }

    emojiCategories.push({title, icon, data});
  }

  return emojiCategories;
};

export const emojiRegexp = /([\uE000-\uF8FF]|\uD83C[\uDF00-\uDFFF]|\uD83D[\uDC00-\uDDFF])/g;

export const preloadEmojiSheet = () => (new Image()).src = emoji.img_sets[emoji.img_set].sheet;

export const escapeWithEmoji = (text) => {
  emoji.include_title = false;
  emoji.include_text = false;
  return emoji.replace_unified(escape(text));
};

export default {
  emoji,
  emojiRegexp,
  getEmojiCategories,
  preloadEmojiSheet,
  escapeWithEmoji
};
