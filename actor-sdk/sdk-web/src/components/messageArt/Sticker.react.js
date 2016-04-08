/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import convertImage from '../../utils/convertImage';

class Sticker extends Component {
  static propTypes = {
    sticker: PropTypes.object.isRequired,
    onClick: PropTypes.func.isRequired
  };

  constructor(props) {
    super(props);

    this.state = {
      isLoading: true
    };

    convertImage(props.sticker.url).then((url) => {
      this.setState({
        url,
        isLoading: false
      });
    });

    this.onClick = this.onClick.bind(this);
  }

  shouldComponentUpdate(nextProps) {
    return nextProps.sticker === this.props.sticker;
  }

  onClick() {
    this.props.onClick(this.props.sticker);
  }

  render() {
    const { isLoading, url } = this.state;

    if (isLoading) {
      return null;
    }

    return (
      <div className="sticker" onClick={this.onClick}>
        <img src={url} />
      </div>
    );
  }
}

export default Sticker;
