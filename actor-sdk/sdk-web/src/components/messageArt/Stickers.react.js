/*
 * Copyright (C) 2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';

import Sticker from './Sticker.react';

class Stickers extends Component {
  static propTypes = {
    stickers: PropTypes.array.isRequired,
    onStickerSelect: PropTypes.func.isRequired
  }

  renderStickers() {
    const { stickers, onStickerSelect } = this.props;
    if (stickers.length === 0) return null;

    return stickers.map((sticker, index) => (
      <Sticker
        sticker={sticker}
        onClick={onStickerSelect}
        key={index}
      />
    ));
  }

  render() {
    return (
      <div className="stickers">
        {this.renderStickers()}
      </div>
    );
  }
}

export default Stickers;
