/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, { Component } from 'react';
import classnames from 'classnames';
import { Path } from 'constants/ActorAppConstants';
import EmojiUtils from 'utils/EmojiUtils';

export default class EmojiDropdown extends Component {
  static propTypes = {
    isOpen: React.PropTypes.bool.isRequired,
    onClose: React.PropTypes.func.isRequired,
    onSelect: React.PropTypes.func.isRequired
  };

  constructor(props) {
    super(props);

    this.state = {
      isOpen: props.isOpen
    }
  }

  componentWillReceiveProps(props) {
    const { isOpen } = props;
    this.setState({isOpen});

    if (isOpen) {
      document.addEventListener('click', this.onClose, false);
    } else {
      document.removeEventListener('click', this.onClose, false);
    }
  }

  onClose = () => this.props.onClose();
  onSelect = (emoji) => this.props.onSelect(emoji);

  render() {
    const { isOpen } = this.state;

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

      emojiCategories.push(<li className="emoji-dropdown__header__tabs__tab">{categoryTabContent}</li>);

      emojis.push(
        <div ref={category}>
          <p>{categoryTitle}</p>
          {categorizedEmoji}
        </div>
      );
    }

    if (isOpen) {
      return (
        <div className={emojiDropdownClassName}>
          <div className="emoji-dropdown__wrapper">
            <header className="emoji-dropdown__header">
              Emoji

              <ul className="emoji-dropdown__header__tabs pull-right">
                {emojiCategories}
              </ul>

            </header>
            <div className="emoji-dropdown__body">
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
