/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { lightbox } from '../../../utils/ImageUtils';

const MAX_WIDTH = 300;
const MAX_HEIGHT = 400;

class Photo extends Component {
  static propTypes = {
    fileUrl: PropTypes.string,
    w: PropTypes.number.isRequired,
    h: PropTypes.number.isRequired,
    preview: PropTypes.string.isRequired,
    isUploading: PropTypes.bool.isRequired
  };

  onClick(event) {
    event.preventDefault();
    lightbox.open(event.target.src, 'message');
  }

  getDimentions() {
    const { w: width, h: height } = this.props;
    if (width > height) {
      if (width > MAX_WIDTH) {
        return {
          width: MAX_WIDTH,
          height: height * (MAX_WIDTH / width)
        };
      }
    } else if (height > MAX_HEIGHT) {
      return {
        width: width * (MAX_HEIGHT / height),
        height: MAX_HEIGHT
      };
    }

    return { width, height };
  }


  render() {
    const { fileUrl, preview } = this.props;
    const { width, height } = this.getDimentions();

    return (
      <img
        className="message__photo"
        src={fileUrl || preview}
        width={width}
        height={height}
        onClick={this.onClick}
      />
    );
  }
}

export default Photo;
