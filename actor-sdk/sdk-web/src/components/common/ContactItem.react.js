/*
 * Copyright (C) 2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { shouldComponentUpdate } from 'react-addons-pure-render-mixin';
import classnames from 'classnames';

import AvatarItem from './AvatarItem.react';

import { escapeWithEmoji } from '../../utils/EmojiUtils';

class ContactItem extends Component {
  static propTypes = {
    uid: PropTypes.number.isRequired,
    name: PropTypes.string.isRequired,
    placeholder: PropTypes.string.isRequired,
    avatar: PropTypes.string,

    children: PropTypes.node
  };

  constructor(props) {
    super(props);

    this.shouldComponentUpdate = shouldComponentUpdate.bind(this);
  }

  render() {
    const { uid, name, placeholder, avatar, children } = this.props;

    return (
      <div className="contact row middle-xs">
        <div className="contact__avatar">
          <AvatarItem
            image={avatar}
            placeholder={placeholder}
            size="small"
            title={name}
          />
        </div>

        <div className="contact__body col-xs">
          <span className="title" dangerouslySetInnerHTML={{__html: escapeWithEmoji(name)}}/>
        </div>

        <div className="contact__controls">
          {children}
        </div>
      </div>
    );
  }
}

export default ContactItem;
