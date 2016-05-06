/*
 * Copyright (C) 2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { Element, Link } from 'react-scroll';
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
    this.onTabMouseEnter = this.onTabMouseEnter.bind(this);
  }

  shouldComponentUpdate(nextProps, nextState) {
    return nextState.title !== this.state.title;
  }

  onSetActive(title) {
    this.setState({ title });
  }

  onTabMouseEnter(event) {
    event.stopPropagation();
    event.preventDefault();
    event.target.click();
  }

  render() {
    const { title } = this.state;

    const emojis = [];
    const emojiTabs = [];

    emojiData.forEach((category, cKey) => {
      emojiTabs.push(
        <Link
          spy
          offset={30}
          duration={300}
          to={category.title}
          key={cKey}
          onSetActive={() => this.onSetActive(category.title)}
          onMouseEnter={this.onTabMouseEnter}
          containerId="emojiContainer"
          className="emojis__header__tabs__tab"
          activeClass="emojis__header__tabs__tab--active"
        >
          <span dangerouslySetInnerHTML={{ __html: category.icon }} />
        </Link>
      );

      const items = category.items.map((item, iKey) => {
        return (
          <span
            key={iKey}
            className="emoji__item"
            onClick={() => this.props.onSelect(item.title)}
            dangerouslySetInnerHTML={{ __html: item.icon }}
          />
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
