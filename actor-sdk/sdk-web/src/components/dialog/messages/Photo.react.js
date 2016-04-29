/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import classnames from 'classnames';

import { lightbox } from '../../../utils/ImageUtils';

let cache = [];

/**
 * Class that represents a component for display photo message content
 * @todo move cache to store;
 */
class Photo extends Component {
  static propTypes = {
    fileUrl: PropTypes.string,
    w: PropTypes.number.isRequired,
    h: PropTypes.number.isRequired,
    preview: PropTypes.string.isRequired,
    isUploading: PropTypes.bool.isRequired,
    className: PropTypes.string,
    loadedClassName: PropTypes.string
  };

  constructor(props) {
    super(props);

    this.state = {
      isImageLoaded: this.isCached()
    };

    this.openLightBox = this.openLightBox.bind(this);
    this.onLoad = this.onLoad.bind(this);
    this.isCached = this.isCached.bind(this);
    this.setCached = this.setCached.bind(this);
    this.getDimentions = this.getDimentions.bind(this);
  }

  openLightBox() {
    lightbox.open(this.props.fileUrl, 'message');
  }

  onLoad() {
    this.setCached();
    if (!this.state.isImageLoaded) {
      this.setState({ isImageLoaded: true });
    }
  }

  isCached() {
    cache[this.props.fileUrl] === true;
  }

  setCached() {
    cache[this.props.fileUrl] = true;
  }

  getDimentions() {
    const { w, h } = this.props;
    const MAX_WIDTH = 300;
    const MAX_HEIGHT = 400;
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

  renderPreview() {
    const { preview } = this.props;

    if (this.isCached()) {
      return null;
    }

    return (
      <img className="photo photo--preview" src={preview}/>
    )
  }

  renderPreloader() {
    const { isUploading } = this.props;
    const { isImageLoaded } = this.state;

    if (this.isCached() || isUploading !== true || isImageLoaded !== false) {
      return null;
    }

    return (
      <div className="preloader"><div/><div/><div/><div/><div/></div>
    );
  }

  renderOriginal() {
    const { fileUrl, w, h } = this.props;

    if (!fileUrl) {
      return null;
    }

    return (
      <img
        className="photo photo--original"
        height={h}
        onClick={this.openLightBox}
        onLoad={this.onLoad}
        src={fileUrl}
        width={w}
      />
    );
  }

  render() {
    const { className, loadedClassName } = this.props;
    const { isImageLoaded } = this.state;

    const imageClassName = isImageLoaded ? classnames(className, loadedClassName) : className;

    return (
      <div className={imageClassName} style={this.getDimentions()}>
        {this.renderPreview()}
        {this.renderOriginal()}
        {this.renderPreloader()}
        <svg dangerouslySetInnerHTML={{ __html: '<filter id="blur-effect"><feGaussianBlur stdDeviation="3"/></filter>' }}/>
      </div>
    );
  }
}

export default Photo;
