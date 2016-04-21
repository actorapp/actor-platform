/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import ActorClient from '../../../utils/ActorClient';

import { processEmojiText } from '../../../utils/EmojiUtils';

function processText(text) {
  let processedText = text;
  processedText = ActorClient.renderMarkdown(processedText);
  processedText = processEmojiText(processedText);
  processedText = processedText.replace(/(@[a-z0-9_]+)/ig, '<span class="message__mention">$1</span>');

  return processedText;
}

class Text extends Component {
  static propTypes = {
    text: PropTypes.string.isRequired,
    className: PropTypes.string
  };

  render() {
    const { text, className } = this.props;

    return (
      <div className={className}>
        <div className="text" dangerouslySetInnerHTML={{ __html: processText(text) }}/>
      </div>
    );
  }
}

export default Text;
