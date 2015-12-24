/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import classnames from 'classnames';

/**
 * Class that represents a component for display sticker message content
 */
class Sticker extends Component {
  static propTypes = {
    className: PropTypes.string,
    fileUrl: PropTypes.string.isRequired,
    h: PropTypes.number.isRequired,
    w: PropTypes.number.isRequired,
  };

  constructor(props) {
    super(props);
    console.debug(props);
    this.state = {
      isLoaded: false
    }
  }

  render() {
    const { className, w, h, fileUrl} = this.props;
    const { isLoaded } = this.state;
    const preloader = <div className="preloader"><div/><div/><div/><div/><div/></div>;
    const stickerClassName = classnames('sticker', {
      'sticker--loaded': isLoaded
    });

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

    return (
      <div className={className}>
        <div className={stickerClassName} style={{width, height}}>
          {preloader}
          <img src={fileUrl} width={width} height={height} onLoad={() => this.setState({isLoaded: true})}/>
        </div>
      </div>
    );
  }
}

export default Sticker;
