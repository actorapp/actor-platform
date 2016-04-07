/*
 * Copyright (C) 2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { emoji, getEmojiCategories } from '../../utils/EmojiUtils';
import { Element, Link } from 'react-scroll';

let emojiTabs = [];
let emojis = [];

class Emojis extends Component {
  static propTypes = {
    onSelect: PropTypes.func.isRequired
  }

  constructor(props) {
    super(props);

    this.state = {
      dropdownTitle: ''
    }

    const emojiCategories = getEmojiCategories();

    emojiCategories.forEach((category, index) => {
      let currentCategoryEmojis = [];

      emoji.change_replace_mode('css');
      const categoryIcon = emoji.replace_colons(category.icon);

      emojiTabs.push(
        <Link
          to={category.title}
          spy
          offset={30}
          duration={300}
          key={index}
          onSetActive={() => this.changeDropdownTitle(category.title)}
          onMouseEnter={this.handleEmojiTabMouseEnter}
          containerId="emojiContainer"
          className="emojis__header__tabs__tab"
          activeClass="emojis__header__tabs__tab--active"
        >
          <span dangerouslySetInnerHTML={{__html: categoryIcon}}/>
        </Link>
      );

      category.data.forEach((emojiChar, index) => {
        emoji.change_replace_mode('css');
        const convertedChar = emoji.replace_unified(emojiChar);
        emoji.colons_mode = true;
        const emojiColon = emoji.replace_unified(emojiChar);
        emoji.colons_mode = false;

        currentCategoryEmojis.push(
          <a onClick={() => props.onSelect(emojiColon)} key={index} dangerouslySetInnerHTML={{__html: convertedChar}}/>
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

  changeDropdownTitle = (title) => this.setState({dropdownTitle: title});

  handleEmojiTabMouseEnter = (event) => {
    event.stopPropagation();
    event.preventDefault();
    event.target.click();
  };

  render() {
    const { dropdownTitle } = this.state;

    return (
      <div className="emojis">
        <header className="emojis__header">
          <p className="emojis__header__title">{dropdownTitle || 'Emoji'}</p>

          <div className="emojis__header__tabs pull-right">
            {emojiTabs}
          </div>
        </header>
        <div className="emojis__body" id="emojiContainer">
          {emojis}
        </div>
      </div>
    )
  }
}

export default Emojis;
