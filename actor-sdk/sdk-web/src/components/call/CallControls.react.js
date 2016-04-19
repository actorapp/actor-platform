/*
* Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
*/

import React, { Component, PropTypes } from 'react';
import { CallStates } from '../../constants/ActorAppConstants';

import AnswerButton from './AnswerButton.react';
import EndButton from './EndButton.react';
import MuteButton from './MuteButton.react';
import CloseButton from './CloseButton.react';
import FullScreenButton from './FullScreenButton.react';
import VideoButton from './VideoButton.react';
import AddUserButton from './AddUserButton.react';

class CallControls extends Component {
  static propTypes = {
    callState: PropTypes.oneOf([
      CallStates.CALLING,
      CallStates.IN_PROGRESS,
      CallStates.CONNECTING,
      CallStates.ENDED
    ]).isRequired,
    small: PropTypes.bool,
    isOutgoing: PropTypes.bool.isRequired,
    isMuted: PropTypes.bool.isRequired,
    onEnd: PropTypes.func.isRequired,
    onAnswer: PropTypes.func.isRequired,
    onMuteToggle: PropTypes.func.isRequired,
    onFullscreen: PropTypes.func.isRequired,
    onUserAdd: PropTypes.func.isRequired,
    onVideo: PropTypes.func.isRequired,
    onClose: PropTypes.func.isRequired
  };

  render() {
    const { isOutgoing, small } = this.props;

    const secondaryControls = [];
    const mainControls = small ? secondaryControls : [];
    switch (this.props.callState) {
      case CallStates.CALLING:
        if (!isOutgoing) {
          mainControls.push(<AnswerButton small={small} onClick={this.props.onAnswer} key="answer" />);
        }

        mainControls.push(
          <EndButton small={small} onClick={this.props.onEnd} key="end" />
        );
        break;
      case CallStates.IN_PROGRESS:
      case CallStates.CONNECTING:
        if (!small) {
          secondaryControls.push(
            <FullScreenButton onClick={this.props.onFullscreen} key="fullscreen" />
          );
        }

        secondaryControls.push(
          <MuteButton value={this.props.isMuted} onToggle={this.props.onMuteToggle} key="mute" />
        );

        if (!small) {
          secondaryControls.push(
            <VideoButton onClick={this.props.onVideo} key="video" />,
            <AddUserButton onClick={this.props.onUserAdd} key="add" />
          );
        }

        mainControls.push(
          <EndButton small={small} onClick={this.props.onEnd} key="end" />
        );
        break;
      case CallStates.ENDED:
        mainControls.push(<CloseButton onClick={this.props.onClose} key="close" />);
        break;
    }

    if (small) {
      return (
        <div className="call__controls">
          <div className="call__controls__icons row top-xs">
            {secondaryControls}
          </div>
        </div>
      );
    }

    return (
      <div className="call__controls">
        <div className="call__controls__icons row top-xs">
          {secondaryControls}
        </div>
        <div className="call__controls__buttons">
          {mainControls}
        </div>
      </div>
    );
  }
}

export default CallControls;
