/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, { Component } from 'react';
import memoize from 'memoizee';
import ActorClient from 'utils/ActorClient';

import { Path } from 'constants/ActorAppConstants';
import { emoji } from 'utils/EmojiUtils';

const processText = (text) => {
  const markedText = ActorClient.renderMarkdown(text);
  const emojifiedText = emoji.replace_unified(emoji.replace_colons(markedText));
  return emojifiedText;
};

const memoizedProcessText = memoize(processText, {
  length: 1000,
  maxAge: 60 * 60 * 1000,
  max: 10000
});

export default class Text extends Component {
  static propTypes = {
    content: React.PropTypes.object.isRequired,
    className: React.PropTypes.string
  };

  constructor(props) {
    super(props);
  }

  render() {
    const { content, className } = this.props;

    return (
      <div className={className}
           dangerouslySetInnerHTML={{__html: memoizedProcessText(content.text)}}>
      </div>
    );
  }
}
