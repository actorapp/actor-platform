/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import memoize from 'memoizee';
import ActorClient from '../../../utils/ActorClient';

import { emoji } from '../../../utils/EmojiUtils';

const processText = (text) => {
  const markedText = ActorClient.renderMarkdown(text);
  let emojifiedText = markedText;

  emoji.include_title = true;
  emoji.include_text = true;
  emoji.change_replace_mode('css');
  emojifiedText = emoji.replace_colons(emojifiedText);
  emojifiedText = emoji.replace_unified(emojifiedText);
  return emojifiedText;
};

const memoizedProcessText = memoize(processText, {
  length: 1,
  maxAge: 60 * 60 * 1000,
  max: 10000
});

/**
 * Class that represents a component for display text message content
 * @param {string} text Message text
 * @param {string} className Component class name
 */
class Text extends Component {
  static propTypes = {
    text: PropTypes.string.isRequired,
    className: PropTypes.string
  };

  constructor(props) {
    super(props);
  }

  render() {
    const { text, className } = this.props;

    return (
      <div className={className}>
        <div className="text" dangerouslySetInnerHTML={{__html: memoizedProcessText(text)}}/>
      </div>
    );
  }
}

export default Text;
