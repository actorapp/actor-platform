/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { map } from 'lodash';
import React, { Component } from 'react';
import classnames from 'classnames';
import { Path } from 'constants/ActorAppConstants';
import EmojiUtils from 'utils/EmojiUtils';

export default class EmojiDropdown extends Component {
  static propTypes = {
    isOpen: React.PropTypes.bool.isRequired,
    onClose: React.PropTypes.func.isRequired,
    onSelect: React.PropTypes.func.isRequired
  };

  constructor(props) {
    super(props);

    this.state = {
      isOpen: props.isOpen
    }
  }

  componentWillReceiveProps(props) {
    const { isOpen } = props;
    this.setState({isOpen: isOpen});

    if (isOpen) {
      document.addEventListener('click', this.onClose, false);
    } else {
      document.removeEventListener('click', this.onClose, false);
    }
  }

  onClose = () => this.props.onClose();
  onSelect = (emoji) => this.props.onSelect(emoji);

  render() {
    const { isOpen } = this.state;

    const emojiChars = map(EmojiUtils.names, (name, index) => {
      const emojiText = `:${name}:`;
      return <img src={EmojiUtils.pathToImage(name)}alt={emojiText} key={index} onClick={() => this.onSelect(emojiText)}/>;
    });

    const emojiDropdownClassName = classnames('emoji-dropdown', {
      'emoji-dropdown--opened': isOpen
    });

    if (isOpen) {
      return (
        <div className={emojiDropdownClassName}>
          <div className="emoji-dropdown__wrapper">
            <header className="emoji-dropdown__header">Emoji</header>
            <div className="emoji-dropdown__body">
              {emojiChars}
            </div>
          </div>
        </div>
      );
    } else {
      return null;
    }
  }
}
