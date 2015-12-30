/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { forEach } from 'lodash';
import React, { Component, PropTypes } from 'react';
import { Container } from 'flux/utils';
import classnames from 'classnames';
import { Path, KeyCodes } from '../../constants/ActorAppConstants';
import { emoji, getEmojiCategories } from '../../utils/EmojiUtils';
import isInside from '../../utils/isInside';
import EmojiActionCreators from '../../actions/EmojiActionCreators'

import EmojiStore from '../../stores/EmojiStore'

import { Element, Link } from 'react-scroll';

let emojiTabs = [];
let emojis = [];
let closeTimer;
const CLOSE_TIMEOUT = 550;

class EmojiDropdown extends Component {
  static propTypes = {
    onSelect: PropTypes.func.isRequired
  };

  static getStores = () => [EmojiStore];

  static calculateState() {
    return {
      isOpen: EmojiStore.isOpen()
    };
  }

  constructor(props) {
    super(props);

    const emojiCategories = getEmojiCategories();

    forEach(emojiCategories, (category, index) => {
      let currentCategoryEmojis = [];

      emoji.change_replace_mode('css');
      const categoryIcon = emoji.replace_colons(category.icon);

      emojiTabs.push(
        <Link to={category.title}
              spy
              offset={30}
              duration={300}
              key={index}
              onSetActive={() => this.changeDropdownTitle(category.title)}
              containerId="emojiContainer"
              onMouseEnter={this.handleEmojiTabMouseEnter}
              className="emoji-dropdown__header__tabs__tab"
              activeClass="emoji-dropdown__header__tabs__tab--active">
          <span dangerouslySetInnerHTML={{__html: categoryIcon}}/>
        </Link>
      );

      forEach(category.data, (emojiChar, index) => {
        emoji.change_replace_mode('css');
        const convertedChar = emoji.replace_unified(emojiChar);
        emoji.colons_mode = true;
        const emojiColon = emoji.replace_unified(emojiChar);
        emoji.colons_mode = false;

        currentCategoryEmojis.push(
          <a onClick={() => this.onSelect(emojiColon)} key={index} dangerouslySetInnerHTML={{__html: convertedChar}}/>
        );
      });

      emojis.push(
        <Element name={category.title} key={index}>
          <p>{category.title}</p>
          {currentCategoryEmojis}
        </Element>
      );
    });
  }

  componentWillUpdate(nextProps, nextState) {
    const { isOpen } = nextState;
    const emojiDropdown = React.findDOMNode(this.refs.emojiDropdown);

    if (isOpen) {
      emojiDropdown.addEventListener('mouseenter', this.handleEmojiMouseEnter, false);
      emojiDropdown.addEventListener('mouseleave', this.handleEmojiMouseLeave, false);
      document.addEventListener('click', this.onDocumentClick, false);
      document.addEventListener('keydown', this.onKeyDown, false);
    } else {
      emojiDropdown.removeEventListener('mouseenter', this.handleEmojiMouseEnter, false);
      emojiDropdown.removeEventListener('mouseleave', this.handleEmojiMouseLeave, false);
      document.removeEventListener('click', this.onDocumentClick, false);
      document.removeEventListener('keydown', this.onKeyDown, false);
    }
  }

  onKeyDown = () => {
    if (event.keyCode === KeyCodes.ESC) {
      event.preventDefault();
      this.handleClose();
    }
  };

  handleClose = () => EmojiActionCreators.close();

  onSelect = (emoji) => this.props.onSelect(emoji);

  onDocumentClick = (event) => {
    event.stopPropagation();
    event.preventDefault();
    if (!event.target.className.includes('emoji-dropdown__header__tabs__tab')) {
      const emojiDropdown = React.findDOMNode(this.refs.emojiDropdown);
      const emojiRect = emojiDropdown.getBoundingClientRect();
      const coords = {
        x: event.pageX || event.clientX,
        y: event.pageY || event.clientY
      };

      if (!isInside(coords, emojiRect)) {
        this.handleClose();
      }
    }
  };

  changeDropdownTitle = (title) => this.setState({dropdownTitle: title});

  handleEmojiTabMouseEnter = (event) => {
    event.stopPropagation();
    event.preventDefault();
    event.target.click();
  };

  handleEmojiOpenerMouseEnter = () => {
    this.handleEmojiMouseEnter();
    localStorage.setItem('isEmojiOpenedBefore', true);
    EmojiActionCreators.open();
  };

  handleEmojiMouseLeave = () => {
    closeTimer = setTimeout(this.handleClose, CLOSE_TIMEOUT)
  };

  handleEmojiMouseEnter = () => {
    clearTimeout(closeTimer);
  };

  render() {
    const { isOpen, dropdownTitle } = this.state;
    const isEmojiOpenedBefore = (localStorage.getItem('isEmojiOpenedBefore') === 'true') || false;

    const emojiDropdownClassName = classnames('emoji-dropdown', {
      'emoji-dropdown--opened': isOpen
    });
    const emojiOpenerClassName = classnames('emoji-opener material-icons', {
      'emoji-opener--active': isOpen,
      'emoji-opener--with-dot': !isEmojiOpenedBefore
    });

    return (
      <div className={emojiDropdownClassName}>
        <i className={emojiOpenerClassName}
           onMouseEnter={this.handleEmojiOpenerMouseEnter}
           onMouseLeave={this.handleEmojiMouseLeave}>insert_emoticon</i>


        <div className="emoji-dropdown__wrapper" ref="emojiDropdown">
          <header className="emoji-dropdown__header">
            <p className="emoji-dropdown__header__title">{dropdownTitle || 'Emoji'}</p>

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
  }
}

export default Container.create(EmojiDropdown, {pure: false, withProps: true});
