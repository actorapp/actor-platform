/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import PreferencesStore from '../../../stores/PreferencesStore';
import { getDimentions, lightbox, renderImageToCanvas } from '../../../utils/ImageUtils';

class Animation extends Component {
  static propTypes = {
    fileUrl: PropTypes.string,
    w: PropTypes.number.isRequired,
    h: PropTypes.number.isRequired,
    preview: PropTypes.string.isRequired
  };

  constructor(props) {
    super(props);

    if (!PreferencesStore.isAnimationAutoPlayEnabled()) {
      this.state = {
        playing: false
      };
    }

    this.onClick = this.onClick.bind(this);
    this.onMouseEnter = this.onMouseEnter.bind(this);
    this.onMouseLeave = this.onMouseLeave.bind(this);
  }

  componentDidMount() {
    renderImageToCanvas(this.props.preview, this.refs.canvas);
    this.updateFrameUrl(this.props);
  }

  componentWillReceiveProps(nextProps) {
    this.updateFrameUrl(nextProps);
  }

  updateFrameUrl({ fileUrl }) {
    if (!fileUrl || !this.state) {
      return;
    }

    renderImageToCanvas(fileUrl, this.refs.canvas).catch((e) => {
      console.error(e);
    });
  }

  onClick(event) {
    event.preventDefault();
    lightbox.open(this.props.fileUrl, 'message');
  }

  onMouseEnter() {
    this.setState({ playing: true });
  }

  onMouseLeave() {
    this.setState({ playing: false });
  }

  getDimentions() {
    const { w: width, h: height } = this.props;
    return getDimentions(width, height);
  }

  render() {
    const { width, height } = this.getDimentions();
    const source = this.props.fileUrl || this.props.preview;

    if (!this.state) {
      return (
        <img
          className="message__photo"
          src={source}
          width={width}
          height={height}
          onClick={this.onClick}
        />
      );
    }

    const { playing } = this.state;
    const canvasStyle = { width, height };
    if (playing) {
      canvasStyle.display = 'none';
    }

    return (
      <div>
        <img
          className="message__photo"
          src={source}
          width={width}
          height={height}
          style={playing ? {} : { display: 'none' }}
          onClick={this.onClick}
          onMouseEnter={this.onMouseEnter}
          onMouseLeave={this.onMouseLeave}
        />
        <canvas
          className="message__photo"
          ref="canvas"
          width={width}
          height={height}
          style={canvasStyle}
          onMouseEnter={this.onMouseEnter}
          onMouseLeave={this.onMouseLeave}
        />
      </div>
    );
  }
}

export default Animation;
