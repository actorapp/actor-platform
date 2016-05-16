/*
 * Copyright (C) 2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { Element } from 'actor-react-scroll';
import EmojiTab from './EmojiTab.react';
import EmojiItem from './EmojiItem.react';
import emojiData from './emojiData';

class Emojis extends Component {
  static propTypes = {
    onSelect: PropTypes.func.isRequired
  }

  constructor(props) {
    super(props);

    this.state = {
      title: 'Emoji'
    };

    this.onSetActive = this.onSetActive.bind(this);
  }

  shouldComponentUpdate(nextProps, nextState) {
    return nextState.title !== this.state.title;
  }

  onSetActive(title) {
    this.setState({ title });
  }

  render() {
    const { title } = this.state;

    const emojis = [];
    const emojiTabs = [];

    emojiData.forEach((category, cKey) => {
      emojiTabs.push(
        <EmojiTab key={cKey} category={category} onSelect={this.onSetActive} />
      );

      const items = category.items.map((emoji, iKey) => {
        return (
          <EmojiItem key={iKey} emoji={emoji} onSelect={this.props.onSelect} />
        );
      });

      emojis.push(
        <Element name={category.title} key={cKey}>
          <p>{category.title}</p>
          {items}
        </Element>
      );
    });

    return (
      <div className="emojis">
        <header className="emojis__header">
          <p className="emojis__header__title">
            {title}
          </p>
          <div className="emojis__header__tabs">
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
