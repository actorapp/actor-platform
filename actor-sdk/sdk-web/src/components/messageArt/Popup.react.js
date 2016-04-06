/*
 * Copyright (C) 2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { KeyCodes } from '../../constants/ActorAppConstants';
import classnames from 'classnames';

class Popup extends Component {
  static propTypes = {
    className: PropTypes.string,
    onSelect: PropTypes.func.isRequired,
    onStickerSelect: PropTypes.func.isRequired,
    onClose: PropTypes.func.isRequired,
    onMouseEnter: PropTypes.func.isRequired,
    onMouseLeave: PropTypes.func.isRequired
  }

  constructor(props) {
    super(props);

    this.state = {
      isStickersOpen: true
    };

    // this.handleEmojisTabClick = this.handleEmojisTabClick.bind(this);
    // this.handleStickerTabClick = this.handleStickerTabClick.bind(this);
    this.handleKeyDown = this.handleKeyDown.bind(this);
  }

  // handleEmojisTabClick(event) {
  //
  // }
  //
  // handleStickerTabClick(event) {
  //
  // }

  // componentWillMount() {
  //   console.debug('componentWillMount')
  // }

  componentDidMount() {
    console.debug('componentDidMount')
    document.addEventListener('keydown', this.handleKeyDown, false);
  }

  componentWillUnmount() {
    console.debug('componentWillUnmount');
    document.removeEventListener('keydown', this.handleKeyDown, false);
  }

  handleKeyDown(event) {
    console.debug('handleKeyDown', event);
    if (event.keyCode === KeyCodes.ESC) {
      event.preventDefault();
      this.props.onClose();
    }
  }

  renderBody() {
    return (
      <div className="message-art__popup__body">
        popup body
      </div>
    );
  }

  renderFooter() {
    const { isStickersOpen } = this.state;

    const emojiTabClassName = classnames('tab', {
      'tab--active': !isStickersOpen
    });
    const stickerTabClassName = classnames('tab', {
      'tab--active': isStickersOpen
    });

    return (
      <footer className="message-art__popup__footer">
        <div className={emojiTabClassName}
             onClick={this.handleEmojisTabClick}>
          Emojis
        </div>
        <div className={stickerTabClassName}
             onClick={this.handleStickerTabClick}>
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
        {this.renderBody()}
        {this.renderFooter()}
      </div>
    )
  }
}

export default Popup;
