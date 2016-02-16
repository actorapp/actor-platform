/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import assignDeep from 'assign-deep';
import DelegateContainer from '../utils/DelegateContainer';

import russian from './ru-RU';
import english from './en-US';
import spanish from './es-ES';
import portuguese from './pt-BR';
import chinese from './zh-CN';

import { addLocaleData } from 'react-intl';

import enLocaleData from 'react-intl/lib/locale-data/en';
import ruLocaleData from 'react-intl/lib/locale-data/ru';
import esLocaleData from 'react-intl/lib/locale-data/es';
import ptLocaleData from 'react-intl/lib/locale-data/pt';
import zhLocaleData from 'react-intl/lib/locale-data/zh';

addLocaleData(enLocaleData);
addLocaleData(ruLocaleData);
addLocaleData(esLocaleData);
addLocaleData(ptLocaleData);
addLocaleData(zhLocaleData);

let language = navigator.language.toLocaleLowerCase() || navigator.browserLanguage.toLocaleLowerCase();
if (language === 'zh-cn') language = 'zh';

// Fallback to default language
const defaultLanguage = english;
russian.messages = assignDeep({}, defaultLanguage.messages, russian.messages);
spanish.messages = assignDeep({}, defaultLanguage.messages, spanish.messages);
portuguese.messages = assignDeep({}, defaultLanguage.messages, portuguese.messages);
chinese.messages = assignDeep({}, defaultLanguage.messages, chinese.messages);

// Set language data
const languageData = {
  'default': defaultLanguage,
  'ru': russian,
  'en': english,
  'es': spanish,
  'pt': portuguese,
  'zh': chinese
};

// Extend language data from delegate
export function extendL18n() {
  const delegate = DelegateContainer.get();

  english.messages = delegate.l18n.english ? assignDeep({}, english.messages, delegate.l18n.default.messages, delegate.l18n.english.messages) : english.messages;
  russian.messages = delegate.l18n.russian ? assignDeep({}, russian.messages, delegate.l18n.default.messages, delegate.l18n.russian.messages) : russian.messages;
  spanish.messages = delegate.l18n.spanish ? assignDeep({}, spanish.messages, delegate.l18n.default.messages, delegate.l18n.spanish.messages) : spanish.messages;
  portuguese.messages = delegate.l18n.portuguese ? assignDeep({}, portuguese.messages, delegate.l18n.default.messages, delegate.l18n.portuguese.messages) : portuguese.messages;
  chinese.messages = delegate.l18n.chinese ? assignDeep({}, chinese.messages, delegate.l18n.default.messages, delegate.l18n.chinese.messages) : chinese.messages;
}

export function getIntlData(locale) {
  const lang = locale ? locale : language;
  const currentLanguage = languageData[lang] || languageData[lang.split('-')[0]] || languageData['default']

  const flattenMessages = (nestedMessages, prefix = '') => {
    return Object.keys(nestedMessages).reduce((messages, key) => {
      let value = nestedMessages[key];
      let prefixedKey = prefix ? `${prefix}.${key}` : key;

      if (typeof value === 'string') {
        messages[prefixedKey] = value;
      } else {
        Object.assign(messages, flattenMessages(value, prefixedKey));
      }

      return messages;
    }, {});
  };

  return {
    locale: currentLanguage.locale,
    messages: flattenMessages(currentLanguage.messages)
  }
}
