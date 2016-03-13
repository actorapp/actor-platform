/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { findDOMNode } from 'react-dom';
import hljs from 'highlight.js';
import ActorClient from '../../../utils/ActorClient';

import { processEmojiText } from '../../../utils/EmojiUtils';

function processText(text) {
  let processedText = text;
  processedText = ActorClient.renderMarkdown(processedText);
  processedText = processEmojiText(processedText);

  return processedText;
}

class Text extends Component {
  static propTypes = {
    text: PropTypes.string.isRequired,
    className: PropTypes.string
  };

  componentDidMount() {
    /*
    requestAnimationFrame(() => {
      const node = findDOMNode(this);
      const codeBlocks = node.getElementsByTagName('pre');
      for (let i = 0; i < codeBlocks.length; i++) {
        const codeBlock = codeBlocks[i];
        hljs.highlightBlock(codeBlock.firstChild);
      }
    });
    */
  }

  render() {
    const { text, className } = this.props;

    return (
      <div className={className}>
        <div className="text" dangerouslySetInnerHTML={{__html: processText(text)}}/>
      </div>
    );
  }
}

export default Text;
