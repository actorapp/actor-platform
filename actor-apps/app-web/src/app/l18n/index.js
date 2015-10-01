/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

const language = navigator.language || navigator.browserLanguage;

// Intl polyfill
if (!window.Intl) {
  require('intl');
  switch (language.toLowerCase()) {
    case 'ru':
    case 'ru-ru':
      require('intl/locale-data/jsonp/ru');
      break;
    case 'es':
    case 'es-es':
      require('intl/locale-data/jsonp/es');
      break;
    case 'en':
    case 'en-us':
    default:
      require('intl/locale-data/jsonp/en');
      break;
  }
}

// Set language data
let intlData;
switch (language.toLowerCase()) {
  case 'ru':
  case 'ru-ru':
    intlData = require('./ru-RU');
    break;
  case 'es':
  case 'es-es':
    intlData = require('./es-ES');
    break;
  case 'en':
  case 'en-us':
  default:
    intlData = require('./en-US');
    break;
}

export default { intlData };
