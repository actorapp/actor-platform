/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { forEach } from 'lodash';
import React, { Component } from 'react';
import classnames from 'classnames';
import { Path, KeyCodes } from 'constants/ActorAppConstants';
import { emoji, getEmojiCategories } from 'utils/EmojiUtils';

import { Element, Link } from 'react-scroll';

export default class EmojiDropdown extends Component {
  static propTypes = {
    isOpen: React.PropTypes.bool.isRequired,
    onClose: React.PropTypes.func.isRequired,
    onSelect: React.PropTypes.func.isRequired
  };

  constructor(props) {
    super(props);

    this.state = {
      isOpen: props.isOpen,
      dropdownTitle: 'Emoji',
      emojiCategories: getEmojiCategories()
    };
  }

  componentWillReceiveProps(props) {
    const { isOpen } = props;
    this.setState({isOpen});

    if (isOpen) {
      document.addEventListener('click', this.onDocumentClick, false);
      document.addEventListener('keydown', this.onKeyDown, false);
    } else {
      document.removeEventListener('click', this.onDocumentClick, false);
      document.removeEventListener('keydown', this.onKeyDown, false);
    }
  }

  onKeyDown = () => {
    if (event.keyCode === KeyCodes.ESC) {
      event.preventDefault();
      this.onClose();
    }
  };

  onClose = () => this.props.onClose();
  onSelect = (emoji) => {
    this.onClose();
    this.props.onSelect(emoji)
  };

  onDocumentClick = (event) => {
    const emojiDropdown = React.findDOMNode(this.refs.emojiDropdown);
    const emojiRect = emojiDropdown.getBoundingClientRect();
    const coords = {
      x: event.pageX || event.clientX,
      y: event.pageY || event.clientY
    };

    if (!this.isInside(coords, emojiRect)) {
      this.onClose();
    }
  };

  isInside = (coords, rect) => {
    return (
      coords.x > rect.left &&
      coords.y > rect.top &&
      coords.x < rect.left + rect.width &&
      coords.y < rect.top + rect.height
    )
  };

  changeDropdownTitle = (title) => this.setState({dropdownTitle: title});

  render() {
    const { isOpen, emojiCategories, dropdownTitle } = this.state;

    const emojiDropdownClassName = classnames('emoji-dropdown', {
      'emoji-dropdown--opened': isOpen
    });

    let emojiTabs = [];
    let emojis = [];

    for (let category of emojiCategories) {
      let currentCategoryEmojis = [];

      emoji.change_replace_mode('css');
      const categoryIcon = emoji.replace_colons(category.icon);

      emojiTabs.push(
        <Link to={category.title}
              spy smooth
              offset={30}
              duration={250}
              onSetActive={() => this.changeDropdownTitle(category.title)}
              containerId="emojiContainer"
              className="emoji-dropdown__header__tabs__tab"
              activeClass="emoji-dropdown__header__tabs__tab--active">
          <span dangerouslySetInnerHTML={{__html: categoryIcon}}/>
        </Link>
      );

      for (let emojiChar of category.data) {
        emoji.change_replace_mode('css');
        const convertedChar = emoji.replace_unified(emojiChar);
        emoji.colons_mode = true;
        const emojiColon = emoji.replace_unified(emojiChar);
        emoji.colons_mode = false;

        currentCategoryEmojis.push(
          <a onClick={() => this.onSelect(emojiColon)} dangerouslySetInnerHTML={{__html: convertedChar}}/>
        );
      }

      emojis.push(
        <Element name={category.title}>
          <p>{category.title}</p>
          {currentCategoryEmojis}
        </Element>
      );
    }

    if (isOpen) {
      return (
        <div className={emojiDropdownClassName}>
          <div className="emoji-dropdown__wrapper" ref="emojiDropdown">
            <header className="emoji-dropdown__header">
              <p className="emoji-dropdown__header__title">{dropdownTitle}</p>

              <div className="emoji-dropdown__header__tabs pull-right">
                {emojiTabs}
              </div>
            </header>
            <div className="emoji-dropdown__body" id="emojiContainer">
              {emojis}
            </div>
          </div>
        </div>
      );
    } else {
      return null;
    }
  }
}
