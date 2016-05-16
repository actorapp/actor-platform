/*
 * Copyright (C) 2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { Link } from 'actor-react-scroll';

class EmojiTab extends Component {
  static propTypes = {
    category: PropTypes.object.isRequired,
    onSelect: PropTypes.func.isRequired
  };

  constructor(props) {
    super(props);

    this.onSelect = this.onSelect.bind(this);
  }

  shouldComponentUpdate(nextProps) {
    return nextProps.category !== this.props.category;
  }

  onSelect() {
    this.props.onSelect(this.props.category.title);
  }

  onMouseEnter(event) {
    event.stopPropagation();
    event.preventDefault();
    event.target.click();
  }

  render() {
    const { category } = this.props;

    return (
      <Link
        spy
        offset={30}
        duration={300}
        to={category.title}
        onSetActive={this.onSelect}
        onMouseEnter={this.onMouseEnter}
        containerId="emojiContainer"
        className="emojis__header__tabs__tab"
        activeClass="emojis__header__tabs__tab--active"
      >
        <span dangerouslySetInnerHTML={{ __html: category.icon }} />
      </Link>
    );
  }
}

export default EmojiTab;
