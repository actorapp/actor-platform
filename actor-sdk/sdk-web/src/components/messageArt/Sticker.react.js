/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';

import Image from '../common/Image.react';

class Sticker extends Component {
  static propTypes = {
    sticker: PropTypes.object.isRequired,
    onClick: PropTypes.func.isRequired
  };

  constructor(props) {
    super(props);

    this.onClick = this.onClick.bind(this);
  }

  shouldComponentUpdate(nextProps) {
    return nextProps.sticker === this.props.sticker;
  }

  onClick() {
    this.props.onClick(this.props.sticker);
  }

  render() {
    const { url } = this.props.sticker;

    return (
      <div className="sticker" onClick={this.onClick}>
        <Image src={url} />
      </div>
    );
  }
}

export default Sticker;
