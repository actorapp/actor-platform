/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import _ from 'lodash';

import React from 'react';

import memoize from 'memoizee';

import { Path } from 'constants/ActorAppConstants';
import ActorClient from 'utils/ActorClient';

import { emoji } from 'utils/EmojiUtils';

const processText = (text) => {
  const markedText = ActorClient.renderMarkdown(text);
  const emojifiedText = emoji.replace_unified(markedText);
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
