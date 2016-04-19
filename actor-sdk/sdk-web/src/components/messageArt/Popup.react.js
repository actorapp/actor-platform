/*
 * Copyright (C) 2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { shouldComponentUpdate } from 'react-addons-pure-render-mixin';
import classnames from 'classnames';

import { KeyCodes, MessageArtPopupState } from '../../constants/ActorAppConstants';

import Emojis from './Emojis.react';
import Stickers from './Stickers.react';

class Popup extends Component {
  static propTypes = {
    className: PropTypes.string,
    stickers: PropTypes.array.isRequired,
    onSelect: PropTypes.func.isRequired,
    onStickerSelect: PropTypes.func.isRequired,
    onClose: PropTypes.func.isRequired,
    onMouseEnter: PropTypes.func.isRequired,
    onMouseLeave: PropTypes.func.isRequired
  }

  constructor(props) {
    super(props);

    this.state = {
      currentState: MessageArtPopupState.EMOJI
    };

    this.handleKeyDown = this.handleKeyDown.bind(this);
    this.shouldComponentUpdate = shouldComponentUpdate.bind(this);
  }

  componentDidMount() {
    document.addEventListener('keydown', this.handleKeyDown, false);
  }

  componentWillUnmount() {
    document.removeEventListener('keydown', this.handleKeyDown, false);
  }

  handleKeyDown(event) {
    if (event.keyCode === KeyCodes.ESC) {
      event.preventDefault();
      this.props.onClose();
    }
  }

  handleTabClick(state) {
    this.setState({ currentState: state })
  }

  renderBody() {
    const { currentState } = this.state;

    switch (currentState) {
      case MessageArtPopupState.EMOJI:
        return (
          <Emojis onSelect={this.props.onSelect}/>
        );
      case MessageArtPopupState.STICKER:
        return (
          <Stickers
            stickers={this.props.stickers}
            onStickerSelect={this.props.onStickerSelect}
          />
        );
      default:
        return null;
    }
  }

  renderFooter() {
    const { currentState } = this.state;

    const emojiTabClassName = classnames('tab', {
      'tab--active': currentState === MessageArtPopupState.EMOJI
    });
    const stickerTabClassName = classnames('tab', {
      'tab--active': currentState === MessageArtPopupState.STICKER
    });

    return (
      <footer className="message-art__popup__footer">
        <div className={emojiTabClassName}
             onClick={() => this.handleTabClick(MessageArtPopupState.EMOJI)}>
          Emojis
        </div>
        <div className={stickerTabClassName}
             onClick={() => this.handleTabClick(MessageArtPopupState.STICKER)}>
          Stickers
        </div>
      </footer>
    )
  }

  render() {
    const { onMouseEnter, onMouseLeave } = this.props;

    return (
      <div
        className="message-art__popup"
        onMouseEnter={onMouseEnter}
        onMouseLeave={onMouseLeave}
      >
        <div className="message-art__popup__body">
          {this.renderBody()}
        </div>

        {this.renderFooter()}
      </div>
    )
  }
}

export default Popup;
