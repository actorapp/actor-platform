/*
 * Copyright (C) 2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';

class EmojiItem extends Component {
  static propTypes = {
    emoji: PropTypes.object.isRequired,
    onSelect: PropTypes.func.isRequired
  };

  constructor(props) {
    super(props);

    this.onSelect = this.onSelect.bind(this);
  }

  shouldComponentUpdate(nextProps) {
    return nextProps.emoji !== this.props.emoji;
  }

  onSelect() {
    this.props.onSelect(this.props.emoji.title);
  }

  render() {
    const { emoji } = this.props;

    return (
      <span
        className="emoji__item"
        onClick={this.onSelect}
        dangerouslySetInnerHTML={{ __html: emoji.icon }}
      />
    );
  }
}

export default EmojiItem;
