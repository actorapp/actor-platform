/*
 * Copyright (C) 2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { shouldComponentUpdate } from 'react-addons-pure-render-mixin';

import MessageArtActionCreators from '../../actions/MessageArtActionCreators';

import Trigger from './Trigger.react';
import Popup from './Popup.react';

class MessageArt extends Component {
  static propTypes = {
    isActive: PropTypes.bool.isRequired,
    stickers: PropTypes.array.isRequired,
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
    this.shouldComponentUpdate = shouldComponentUpdate.bind(this);
  }

  onMouseLeave() {
    if (this.openTimeout) {
      clearTimeout(this.openTimeout);
      this.openTimeout = false;
    }

    this.closeTimeout = setTimeout(this.props.onClose, 300);
  }

  onMouseEnter() {
    if (this.closeTimeout) {
      clearTimeout(this.closeTimeout);
      this.closeTimeout = false;
    }

    if (!this.props.isActive) {
      this.openTimeout = setTimeout(this.props.onOpen, 60);
    }
  }

  renderPopup() {
    const { isActive, stickers, onSelect, onStickerSelect, onClose } = this.props;
    if (!isActive) return null;

    return (
      <Popup
        onSelect={onSelect}
        onClose={onClose}
        stickers={stickers}
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
          isDotVisible={false}
        >
          <i className="material-icons">insert_emoticon</i>
        </Trigger>
        {this.renderPopup()}
      </div>
    )
  }
}

export default MessageArt;
