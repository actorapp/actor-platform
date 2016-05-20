/*
 * Copyright (C) 2016 Actor LLC. <https://actor.im>
 */

import { emoji, getEmojiCategories } from '../../utils/EmojiUtils';

const categories = getEmojiCategories();
const data = categories.map((category) => {
  emoji.change_replace_mode('css');

  return {
    title: category.title,
    icon: emoji.replace_colons(category.icon),
    items: category.data.map((char) => {
      emoji.change_replace_mode('css');
      const icon = emoji.replace_unified(char);

      emoji.colons_mode = true;
      const title = emoji.replace_unified(char);
      emoji.colons_mode = false;

      return { title, icon, char };
    })
  };
});

export default data;
