/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import classnames from 'classnames';
import Recorder from 'opus-recorder';

let isRecordingSupported = Recorder.isRecordingSupported() ? true : false;
console.debug('isRecordingSupported', isRecordingSupported);

class VoiceRecorder extends Component {
  static propTypes = {
    onFinish: PropTypes.func.isRequired
  };

  constructor(props) {
    super(props);

    this.state = {
      isRecording: false
    };

    if (isRecordingSupported) {
      this.recorder = new Recorder();
      this.recorder.addEventListener('duration', this.handleChangeDuration);
      this.recorder.addEventListener('streamReady', this.handleStreamReady);
      this.recorder.addEventListener('dataAvailable', this.handleSendRecord);
    }
  }

  handleStartRecord = () => {
    this.recorder.initStream();
  };

  handleStopRecord = () => {
    this.recorder.stop();
    this.setState({ isRecording: false });
  };

  handleSendRecord = (event) => {
    const { onFinish } = this.props;
    const { duration } = this.state;

    onFinish && onFinish(duration * 1000, event.detail); //Duration must be in ms
  };

  handleStreamReady = () => {
    this.recorder.start();
    this.setState({ isRecording: true });
  };

  handleChangeDuration = (event) => this.setState({ duration: event.detail.toFixed(2) });

  render() {
    if (isRecordingSupported) {
      const { isRecording, duration } = this.state;

      const voiceRecorderClassName = classnames('voice-recorder', {
        'voice-recorder--recording': isRecording
      });

      return (
        <div className={voiceRecorderClassName}>
          <i className="material-icons icon"
             onMouseDown={this.handleStartRecord}
             onMouseUp={this.handleStopRecord}>mic</i>
          <div className="duration">
            <div className="fill row middle-xs center-xs">
              Voice message duration:&nbsp; {duration}
            </div>
          </div>
        </div>
      );
    } else {
      return null;
    }
  }
}

export default VoiceRecorder;
