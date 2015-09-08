/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, { Component } from 'react';
import classnames from 'classnames';

export default class EmojiDropdown extends Component {
  static propTypes = {
    isOpen: React.PropTypes.bool,
    onClose: React.PropTypes.func
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

  onClose = () => {
    this.props.onClose();
  };

  render() {
    const { isOpen } = this.state;

    const emojiDropdownClassName = classnames('emoji-dropdown', {
      'emoji-dropdown--opened': isOpen
    });

    if (isOpen) {
      return (
        <div className={emojiDropdownClassName}>
          <div className="emoji-dropdown__wrapper">
            <h4>EmojiDropdown</h4>
          </div>
        </div>
      );
    } else {
      return null;
    }
  }
}
