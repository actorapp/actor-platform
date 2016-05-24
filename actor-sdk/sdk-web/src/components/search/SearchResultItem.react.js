/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import AvatarItem from '../common/AvatarItem.react';
import MessageContent from '../dialog/messages/MessageContent.react';

class SearchResultItem extends Component {
  static propTypes = {
    content: PropTypes.object.isRequired,
    date: PropTypes.string.isRequired,
    sender: PropTypes.object.isRequired
  };

  render() {
    const { content, date, sender } = this.props;

    return (
      <li className="search__results__item search__results__item--message row">
        <AvatarItem
          image={sender.avatar}
          placeholder={sender.placeholder}
          size="small"
          title={sender.title}
        />

        <div className="search__results__item__body col-xs">
          <header>
            <time className="time pull-right">{date}</time>
            <h4 className="title">{sender.title}</h4>
          </header>
          <div className="content">
            <MessageContent content={content} />
          </div>
        </div>
      </li>
    );
  }
}

export default SearchResultItem;
