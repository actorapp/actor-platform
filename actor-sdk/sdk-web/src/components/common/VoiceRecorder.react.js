/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import classNames from 'classnames';
import Recorder from 'opus-recorder';

const isRecordingSupported = Recorder.isRecordingSupported();

class VoiceRecorder extends Component {
  static propTypes = {
    onFinish: PropTypes.func.isRequired
  };

  constructor(props) {
    super(props);

    this.state = {
      duration: 0,
      isRecording: false
    };

    this.onRecordStart = this.onRecordStart.bind(this);
    this.onRecordStop = this.onRecordStop.bind(this);
    this.onStreamReady = this.onStreamReady.bind(this);
    this.onRecordDone = this.onRecordDone.bind(this);
    this.onDurationChange = this.onDurationChange.bind(this);
  }

  shouldComponentUpdate(nextProps, nextState) {
    return nextState.isRecording !== this.state.isRecording ||
           nextState.duration !== this.state.duration;
  }

  componentDidMount() {
    if (isRecordingSupported) {
      this.recorder = new Recorder();
      this.recorder.addEventListener('duration', this.onDurationChange);
      this.recorder.addEventListener('streamReady', this.onStreamReady);
      this.recorder.addEventListener('dataAvailable', this.onRecordDone);
    }
  }

  componentWillUnmount() {
    this.recorder.removeEventListener('duration', this.onDurationChange);
    this.recorder.removeEventListener('streamReady', this.onStreamReady);
    this.recorder.removeEventListener('dataAvailable', this.onRecordDone);
    this.recorder = null;
  }

  onRecordStart() {
    this.recorder.initStream();
  }

  onRecordStop() {
    this.recorder.stop();
    this.setState({ isRecording: false, duration: 0 });
  }

  onStreamReady() {
    this.recorder.start();
    this.setState({ isRecording: true });
  }

  onRecordDone(event) {
    // duration must be in ms
    const duration = this.state.duration * 1000;
    if (duration >= 100) {
      this.props.onFinish(duration, event.detail);
    }
  }

  onDurationChange(event) {
    const duration = event.detail.toFixed(2);
    this.setState({ duration });
  }

  renderDuration() {
    if (!this.state.duration) {
      return null;
    }

    return (
      <div className="voice-recorder__duration">
        <div className="fill row middle-xs center-xs">
          Voice message duration:&nbsp; {this.state.duration}
        </div>
      </div>
    );
  }

  render() {
    if (!isRecordingSupported) {
      return null;
    }

    const className = classNames('voice-recorder__icon', {
      'voice-recorder__icon--active': this.state.isRecording
    });

    return (
      <div className="voice-recorder">
        <span className={className} onMouseDown={this.onRecordStart} onMouseUp={this.onRecordStop}>
          <i className="material-icons">mic</i>
        </span>
        {this.renderDuration()}
      </div>
    );
  }
}

export default VoiceRecorder;
