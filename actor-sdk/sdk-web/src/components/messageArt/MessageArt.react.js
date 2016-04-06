/*
 * Copyright (C) 2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';

import MessageArtActionCreators from '../../actions/MessageArtActionCreators';

import Trigger from './Trigger.react';
import Popup from './Popup.react';

class MessageArt extends Component {
  constructor(props) {
    super(props);

    this.openMessageArtPopup = MessageArtActionCreators.open.bind(this);
    this.closeMessageArtPopup = MessageArtActionCreators.close.bind(this);
  }
  static propTypes = {
    isActive: PropTypes.bool.isRequired,
    onSelect: PropTypes.func.isRequired,
    onStickerSelect: PropTypes.func.isRequired
  }

  renderPopup() {
    const { onSelect, onStickerSelect, isActive } = this.props;
    if (!isActive) return null;

    return (
      <Popup
        onSelect={onSelect}
        onStickerSelect={onStickerSelect}
        onClose={this.closeMessageArtPopup}
      />
    );
  }

  render() {
    const { isActive } = this.props;

    return (
      <div className="message-art">

        <Trigger onMouseEnter={this.openMessageArtPopup}
                 isActive={isActive}
                 isDotVisible>
          <i className="material-icons">insert_emoticon</i>
        </Trigger>

        {this.renderPopup()}
      </div>
    )
  }
}

export default MessageArt;
