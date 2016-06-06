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
    if (this.state) {
      renderImageToCanvas(this.props.preview, this.refs.canvas).catch((e) => {
        console.error(e);
      });
    }
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

  renderImage(source, width, height, playing) {
    if (!playing) {
      return null;
    }

    return (
      <img
        src={source}
        width={width}
        height={height}
        onClick={this.onClick}
        onMouseEnter={this.onMouseEnter}
        onMouseLeave={this.onMouseLeave}
      />
    );
  }

  renderCanvas(width, height, playing) {
    const style = { width, height };
    if (playing) {
      // Hide using style because DOM node required by renderImageToCanvas
      style.display = 'none';
    }

    return (
      <canvas
        ref="canvas"
        style={style}
        onMouseEnter={this.onMouseEnter}
        onMouseLeave={this.onMouseLeave}
      />
    );
  }

  renderState(playing) {
    const glyph = playing ? 'play_circle_outline' : 'pause_circle_outline';

    return (
      <i className="material-icons message__animation__state">{glyph}</i>
    );
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

    return (
      <div className="message__animation" style={{ width, height }}>
        {this.renderState(playing)}
        {this.renderImage(source, width, height, playing)}
        {this.renderCanvas(width, height, playing)}
      </div>
    );
  }
}

export default Animation;
