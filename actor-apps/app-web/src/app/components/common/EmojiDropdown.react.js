/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { forEach } from 'lodash';
import React, { Component } from 'react';
import classnames from 'classnames';
import { Path, KeyCodes } from 'constants/ActorAppConstants';
import EmojiUtils from 'utils/EmojiUtils';

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
      categoryTitle: 'Emoji'
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

      this.setState({categoryTitle: 'Emoji'});
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

  onSetActive = (category) => {
    switch(category) {
      case 'people':
        this.setState({categoryTitle: 'People'});
        break;
      case 'nature':
        this.setState({categoryTitle: 'Nature'});
        break;
      case 'foodanddrink':
        this.setState({categoryTitle: 'Food & Drink'});
        break;
      case 'celebration':
        this.setState({categoryTitle: 'Celebration'});
        break;
      case 'activity':
        this.setState({categoryTitle: 'Activity'});
        break;
      case 'travelandplaces':
        this.setState({categoryTitle: 'Travel & Places'});
        break;
      case 'objectsandsymbols':
        this.setState({categoryTitle: 'Objects & Symbols'});
        break;
    }
  };

  render() {
    const { isOpen, categoryTitle } = this.state;

    const emojiDropdownClassName = classnames('emoji-dropdown', {
      'emoji-dropdown--opened': isOpen
    });

    const emojiChars = EmojiUtils.categorizedArray();

    let emojiCategories = [];
    let emojis = [];

    for (let category in emojiChars) {
      let categoryTabContent = [];
      let categoryTitle = '';
      let categorizedEmoji = [];

      switch(category) {
        case 'people':
          categoryTitle = 'People';
          categoryTabContent = <img src={EmojiUtils.pathToImage('grinning')} alt={categoryTitle}/>;
          break;
        case 'nature':
          categoryTitle = 'Nature';
          categoryTabContent = <img src={EmojiUtils.pathToImage('evergreen_tree')} alt={categoryTitle}/>;
          break;
        case 'foodanddrink':
          categoryTitle = 'Food & Drink';
          categoryTabContent = <img src={EmojiUtils.pathToImage('hamburger')} alt={categoryTitle}/>;
          break;
        case 'celebration':
          categoryTitle = 'Celebration';
          categoryTabContent = <img src={EmojiUtils.pathToImage('gift')} alt={categoryTitle}/>;
          break;
        case 'activity':
          categoryTitle = 'Activity';
          categoryTabContent = <img src={EmojiUtils.pathToImage('football')} alt={categoryTitle}/>;
          break;
        case 'travelandplaces':
          categoryTitle = 'Travel & Places';
          categoryTabContent = <img src={EmojiUtils.pathToImage('airplane')} alt={categoryTitle}/>;
          break;
        case 'objectsandsymbols':
          categoryTitle = 'Objects & Symbols';
          categoryTabContent = <img src={EmojiUtils.pathToImage('eyeglasses')} alt={categoryTitle}/>;
          break;
      }

      for (let emoji in emojiChars[category]) {
        const emojiText = `:${emoji}:`;
        categorizedEmoji.push(
          <img src={EmojiUtils.pathToImage(emoji)} alt={emojiText} onClick={() => this.onSelect(emojiText)}/>
        );
      }

      emojiCategories.push(
        <Link to={category}
              spy smooth
              offset={30}
              duration={250}
              onSetActive={this.onSetActive}
              containerId="emojiContainer"
              className="emoji-dropdown__header__tabs__tab"
              activeClass="emoji-dropdown__header__tabs__tab--active">
          {categoryTabContent}
        </Link>
      );

      emojis.push(
        <Element name={category}>
          <p>{categoryTitle}</p>
          {categorizedEmoji}
        </Element>
      );
    }

    if (isOpen) {
      return (
        <div className={emojiDropdownClassName}>
          <div className="emoji-dropdown__wrapper" ref="emojiDropdown">
            <header className="emoji-dropdown__header">
              {categoryTitle}

              <div className="emoji-dropdown__header__tabs pull-right">
                {emojiCategories}
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
