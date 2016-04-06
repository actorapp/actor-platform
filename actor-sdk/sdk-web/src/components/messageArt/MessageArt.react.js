/*
 * Copyright (C) 2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';

import MessageArtActionCreators from '../../actions/MessageArtActionCreators';

import Trigger from './Trigger.react';
import Popup from './Popup.react';

class MessageArt extends Component {
  static propTypes = {
    isActive: PropTypes.bool.isRequired,
    onOpen: PropTypes.func.isRequired,
    onClose: PropTypes.func.isRequired,
    onSelect: PropTypes.func.isRequired,
    onStickerSelect: PropTypes.func.isRequired
  }

  static defaultProps = {
    onOpen: MessageArtActionCreators.open,
    onClose: MessageArtActionCreators.close
  }

  constructor(props) {
    super(props);

    this.onMouseLeave = this.onMouseLeave.bind(this);
    this.onMouseEnter = this.onMouseEnter.bind(this);
  }

  onMouseLeave() {
    this.closeTimeout = setTimeout(this.props.onClose, 300);
  }

  onMouseEnter() {
    if (this.closeTimeout) {
      clearTimeout(this.closeTimeout);
      this.closeTimeout = false;
    }

    if (!this.props.isActive) {
      this.props.onOpen();
    }
  }

  renderPopup() {
    const { onSelect, onStickerSelect, isActive } = this.props;
    if (!isActive) return null;

    return (
      <Popup
        onSelect={onSelect}
        onStickerSelect={onStickerSelect}
        onMouseEnter={this.onMouseEnter}
        onMouseLeave={this.onMouseLeave}
      />
    );
  }

  render() {
    const { isActive } = this.props;

    return (
      <div className="message-art">

        <Trigger
          onMouseEnter={this.onMouseEnter}
          onMouseLeave={this.onMouseLeave}
          isActive={isActive}
          isDotVisible
        >
          <i className="material-icons">insert_emoticon</i>
        </Trigger>

        {this.renderPopup()}
      </div>
    )
  }
}

export default MessageArt;
