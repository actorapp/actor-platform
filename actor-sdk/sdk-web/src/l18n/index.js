/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import DelegateContainer from '../utils/DelegateContainer'
import russian from './ru-RU';
import english from './en-US';
import spanish from './es-ES';
import portuguese from './pt-BR';
import chinese from './zh-CN';

let language = navigator.language.toLocaleLowerCase() || navigator.browserLanguage.toLocaleLowerCase();

if (language === 'zh-cn') {
  language = 'zh'
}

// Set language data
const languageData = {
  'ru': russian,
  'en': english,
  'es': spanish,
  'pt': portuguese,
  'zh': chinese
};

// Extend language data from delegate
export function extendL18n() {
  const delegate = DelegateContainer.get();

  english.messages = delegate.l18n.english ? Object.assign(english.messages, delegate.l18n.english.messages) : english.messages;
  russian.messages = delegate.l18n.russian ? Object.assign(russian.messages, delegate.l18n.russian.messages) : russian.messages;
  spanish.messages = delegate.l18n.spanish ? Object.assign(spanish.messages, delegate.l18n.spanish.messages) : spanish.messages;
  portuguese.messages = delegate.l18n.portuguese ? Object.assign(portuguese.messages, delegate.l18n.portuguese.messages) : portuguese.messages;
  chinese.messages = delegate.l18n.chinese ? Object.assign(chinese.messages, delegate.l18n.chinese.messages) : chinese.messages;
}

export function getIntlData() {
  return languageData[language] || languageData[language.split('-')[0]] || languageData['en'];
}
