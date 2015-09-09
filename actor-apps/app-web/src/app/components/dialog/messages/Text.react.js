/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import _ from 'lodash';

import React from 'react';

import memoize from 'memoizee';
import emojify from 'emojify.js';
import emojiCharacters from 'emoji-named-characters';

import { Path } from 'constants/ActorAppConstants';
import ActorClient from 'utils/ActorClient';

const inversedEmojiCharacters = _.invert(_.mapValues(emojiCharacters, (e) => e.character));

emojify.setConfig({
  mode: 'img',
  img_dir: Path.toEmoji // eslint-disable-line
});

const emojiVariants = _.map(Object.keys(inversedEmojiCharacters), (name) => name.replace(/\+/g, '\\+'));

const emojiRegexp = new RegExp('(' + emojiVariants.join('|') + ')', 'gi');

const processText = function (text) {
  const markedText = ActorClient.renderMarkdown(text);

  // need hack with replace because of https://github.com/Ranks/emojify.js/issues/127
  const noPTag = markedText.replace(/<p>/g, '<p> ');

  let emojifiedText = emojify
    .replace(noPTag.replace(emojiRegexp, (match) => `:${inversedEmojiCharacters[match]}:`));

  return emojifiedText;
};

const memoizedProcessText = memoize(processText, {
  length: 1000,
  maxAge: 60 * 60 * 1000,
  max: 10000
});

class Text extends React.Component {
  static propTypes = {
    content: React.PropTypes.object.isRequired,
    className: React.PropTypes.string
  };

  constructor(props) {
    super(props);
  }

  render() {
    const { content, className } = this.props;

    const renderedContent = (
        <div className={className}
             dangerouslySetInnerHTML={{__html: memoizedProcessText(content.text)}}>
        </div>
      );

    return renderedContent;
  }
}

export default Text;
