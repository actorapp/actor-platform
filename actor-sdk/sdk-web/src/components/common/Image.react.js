/*
 * Copyright (C) 2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';

import convertImage from '../../utils/convertImage';

class Image extends Component {
  static propTypes = {
    src: PropTypes.string.isRequired,
    width: PropTypes.number,
    height: PropTypes.number,
    onLoad: PropTypes.func,
    onError: PropTypes.func,
    onClick: PropTypes.func
  }

  constructor(props) {
    super(props);

    this.state = {
      isLoading: true
    };

    convertImage(props.src).then((src) => {
      this.setState({
        src,
        isLoading: false
      });
    });
  }

  render() {
    const { isLoading, src } = this.state;
    if (isLoading) return null;

    return (
      <img {...this.props} src={src} />
    );
  }
}

export default Image;
