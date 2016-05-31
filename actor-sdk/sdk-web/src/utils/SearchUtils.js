/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import { escapeRegExp } from 'lodash';

function queryToRegExp(query) {
  const pattern = '^' + escapeRegExp(query.trim()).replace(/\s+/g, '.*\\s+');

  return new RegExp(pattern, 'i');
}

export function search(query, items, getValues) {
  if (!query) {
    return [];
  }

  const pattern = queryToRegExp(query);
  return items.filter((item) => {
    const values = getValues(item);
    return values.some((value) => pattern.test(value));
  });
}
