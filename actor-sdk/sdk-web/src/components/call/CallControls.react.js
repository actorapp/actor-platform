/*
* Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
*/

import React, {Component, PropTypes} from 'react';
import { FormattedMessage } from 'react-intl';
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
    const {isOutgoing} = this.props;

    const mainControls = [];
    const secondaryControls = [];
    switch (this.props.callState) {
      case CallStates.CALLING:
        if (!isOutgoing) {
          mainControls.push(<AnswerButton onClick={this.props.onAnswer} key="answer" />);
        }

        mainControls.push(<EndButton onClick={this.props.onEnd} isOutgoing={isOutgoing} key="end" />);
        break;
      case CallStates.IN_PROGRESS:
      case CallStates.CONNECTING:
        secondaryControls.push([
          <FullScreenButton onClick={this.props.onFullscreen} key="fullscreen" />,
          <MuteButton value={this.props.isMuted} onToggle={this.props.onMuteToggle} key="mute" />,
          <VideoButton onClick={this.props.onVideo} key="video" />,
          <AddUserButton onClick={this.props.onUserAdd} key="add" />,
        ]);
        mainControls.push(<EndButton onClick={this.props.onEnd} isOutgoing={isOutgoing} key="end" />);
        break;
      case CallStates.ENDED:
        mainControls.push(<CloseButton onClick={this.props.onClose} key="close" />);
        break;
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
