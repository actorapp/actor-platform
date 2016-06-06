/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { getDimentions, lightbox } from '../../../utils/ImageUtils';

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
    return getDimentions(width, height);
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
