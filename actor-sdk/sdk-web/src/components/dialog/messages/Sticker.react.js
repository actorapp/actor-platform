/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import classnames from 'classnames';
import { shouldComponentUpdate } from 'react-addons-pure-render-mixin';

import Image from '../../common/Image.react';

let cache = {};

/**
 * Class that represents a component for display sticker message content
 */
class Sticker extends Component {
  static propTypes = {
    className: PropTypes.string,
    fileUrl: PropTypes.string,
    h: PropTypes.number.isRequired,
    w: PropTypes.number.isRequired
  };

  constructor(props) {
    super(props);

    this.state = {
      isLoaded: this.isCached(),
      ...this.calculateDementions()
    }

    this.onLoad = this.onLoad.bind(this);
    this.isCached = this.isCached.bind(this);
    this.setCached = this.setCached.bind(this);
    this.shouldComponentUpdate = shouldComponentUpdate.bind(this);
  }

  onLoad() {
    this.setCached();
    if (!this.state.isLoaded) {
      this.setState({ isLoaded: true });
    }
  }

  isCached() {
    return cache[this.props.fileUrl] === true;
  }

  setCached() {
    cache[this.props.fileUrl] = true;
  }

  calculateDementions() {
    const { w, h } = this.props;
    const MAX_WIDTH = 200;
    const MAX_HEIGHT = 200;

    let width = w;
    let height = h;

    if (width > height) {
      if (width > MAX_WIDTH) {
        height *= MAX_WIDTH / width;
        width = MAX_WIDTH;
      }
    } else {
      if (height > MAX_HEIGHT) {
        width *= MAX_HEIGHT / height;
        height = MAX_HEIGHT;
      }
    }

    return { width, height };
  }

  renderPreloader() {
    const { isLoaded } = this.state;
    if (isLoaded) return null;

    return (
      <div className="preloader">
        <div/><div/><div/><div/><div/>
      </div>
    );
  }

  renderSticker() {
    const { fileUrl } = this.props;
    if (!fileUrl) return null;

    const { width, height } = this.state;

    return (
      <Image
        src={fileUrl}
        width={width}
        height={height}
        onLoad={this.onLoad}
      />
    )
  }

  render() {
    const { className } = this.props;
    const { isLoaded, width, height } = this.state;
    const stickerClassName = classnames('sticker', {
      'sticker--loaded': isLoaded
    });

    return (
      <div className={className}>
        <div className={stickerClassName} style={{ width, height }}>
          {this.renderPreloader()}
          {this.renderSticker()}
        </div>
      </div>
    );
  }
}

export default Sticker;
