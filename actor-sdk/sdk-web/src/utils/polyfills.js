/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

export default function initPollyfils(callback) {
  if (!global.Intl) {
    require.ensure(['intl'], (require) => {
      console.debug('Load Intl polyfill');
      require('intl');
      callback && callback();
    }, 'intl-polyfill');
  } else {
    console.debug('Intl polyfill is not required');
    callback && callback();
  }
}
