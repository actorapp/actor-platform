/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { map, uniq, without, forEach, pick } from 'lodash';
import { Path } from 'constants/ActorAppConstants';
import emojilib from 'emojilib';

//const emojiCategories = without(uniq(map(emojilib, (emoji) => emoji.category)), undefined, '_custom');
const emojiCategories = [
  'people', 'nature', 'foodanddrink', 'celebration', 'activity', 'travelandplaces', 'objectsandsymbols'
];



const getCategoryEmojis = (category) => pick(emojilib, (value, key) => {
  if (value.category === category) {
    return key;
  }
});

const categorizedArray = () => {
  let emojiArray = [];

  forEach(emojiCategories, (category) => {
    emojiArray[category] = getCategoryEmojis(category);
  });

  return emojiArray;
};


export default {
  emojis: emojilib,
  categories: emojiCategories,
  categorizedArray,
  getCategoryEmojis,
  pathToImage: (name) => `${Path.toEmoji}/${name}.png`
}
