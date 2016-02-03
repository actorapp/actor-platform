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
    content: PropTypes.object.isRequired,
    className: PropTypes.string,
    loadedClassName: PropTypes.string
  };

  constructor(props) {
    super(props);

    this.state = {
      isImageLoaded: this.isCached()
    };
  }

  openLightBox = () => lightbox.open(this.props.content.fileUrl, 'message');

  onLoad = () => {
    this.setCached();
    if (!this.state.isImageLoaded) {
      this.setState({isImageLoaded: true});
    }
  };

  isCached = () => cache[this.props.content.fileUrl] === true;

  setCached = () => {
    cache[this.props.content.fileUrl] = true;
  };

  render() {
    const { content, className, loadedClassName } = this.props;
    const { isImageLoaded } = this.state;

    const MAX_WIDTH = 300;
    const MAX_HEIGHT = 400;
    let width = content.w;
    let height = content.h;

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

    let original = null,
        preview = null,
        preloader = null;

    if (content.fileUrl) {
      original = (
        <img className="photo photo--original"
             height={content.h}
             onClick={this.openLightBox}
             onLoad={this.onLoad}
             src={content.fileUrl}
             width={content.w}/>
      );
    }

    if (!this.isCached()) {
      preview = <img className="photo photo--preview" src={content.preview}/>;

      if (content.isUploading === true || isImageLoaded === false) {
        preloader = <div className="preloader"><div/><div/><div/><div/><div/></div>;
      }
    }

    const imageClassName = isImageLoaded ? classnames(className, loadedClassName) : className;

    return (
      <div className={imageClassName} style={{width, height}}>
        {preview}
        {original}
        {preloader}
        <svg dangerouslySetInnerHTML={{__html: '<filter id="blur-effect"><feGaussianBlur stdDeviation="3"/></filter>'}}/>
      </div>
    );
  }
}

export default Photo;
