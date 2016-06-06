/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import classNames from 'classnames';
import PreferencesStore from '../../../stores/PreferencesStore';
import { getDimentions, renderImageToCanvas } from '../../../utils/ImageUtils';

class Animation extends Component {
  static propTypes = {
    fileUrl: PropTypes.string,
    w: PropTypes.number.isRequired,
    h: PropTypes.number.isRequired,
    preview: PropTypes.string.isRequired
  };

  constructor(props) {
    super(props);

    this.state = {
      playing: PreferencesStore.isAnimationAutoPlayEnabled()
    };

    this.onClick = this.onClick.bind(this);
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
    this.setState({ playing: !this.state.playing });
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
        onClick={this.onClick}
      />
    );
  }

  renderState(playing) {
    const glyph = playing ? 'pause_circle_outline' : 'play_circle_outline';

    const className = classNames('material-icons message__animation__state', {
      'message__animation__state--playing': playing
    });

    return (
      <i className={className} onClick={this.onClick}>{glyph}</i>
    );
  }

  render() {
    const { playing } = this.state;
    const { width, height } = this.getDimentions();
    const source = this.props.fileUrl || this.props.preview;

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
