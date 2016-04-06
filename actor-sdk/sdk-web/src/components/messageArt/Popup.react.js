/*
 * Copyright (C) 2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { findDOMNode } from 'react-dom';
import { KeyCodes } from '../../constants/ActorAppConstants';
import classnames from 'classnames';
import isInside from '../../utils/isInside';

class Popup extends Component {
  static propTypes = {
    className: PropTypes.string,
    onSelect: PropTypes.func.isRequired,
    onStickerSelect: PropTypes.func.isRequired,
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
    this.handlePopupClose = this.handlePopupClose.bind(this);
    this.handleKeyDown = this.handleKeyDown.bind(this);
    this.handleDocumentClick = this.handleDocumentClick.bind(this);
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
    const popupNode = findDOMNode(this.refs.popup);

    popupNode.addEventListener('mouseenter', this.handlePopupMouseEnter, false);
    popupNode.addEventListener('mouseleave', this.handlePopupMouseLeave, false);
    document.addEventListener('click', this.handleDocumentClick, false);
    document.addEventListener('keydown', this.handleKeyDown, false);
  }

  componentWillUnmount() {
    console.debug('componentWillUnmount');
    const popupNode = findDOMNode(this.refs.popup);

    popupNode.removeEventListener('mouseenter', this.handlePopupMouseEnter, false);
    popupNode.removeEventListener('mouseleave', this.handlePopupMouseLeave, false);
    document.removeEventListener('click', this.handleDocumentClick, false);
    document.removeEventListener('keydown', this.handleKeyDown, false);
  }

  handlePopupClose() {
    console.debug('handlePopupClose');
    this.props.onClose();
  }

  handleDocumentClick(event) {
    console.debug('handleDocumentClick', event);
    event.stopPropagation();
    event.preventDefault();

    const popupRect = findDOMNode(this.refs.popup).getBoundingClientRect();

    const coords = {
      x: event.pageX || event.clientX,
      y: event.pageY || event.clientY
    };

    if (!isInside(coords, popupRect)) this.handlePopupClose();
  }

  handleKeyDown(event) {
    console.debug('handleKeyDown', event);
    if (event.keyCode === KeyCodes.ESC) {
      event.preventDefault();
      this.handlePopupClose();
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
        ref="popup"
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
