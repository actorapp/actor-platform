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
    onEnd: PropTypes.func.isRequired,
    onAnswer: PropTypes.func.isRequired,
    onMute: PropTypes.func.isRequired,
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
        secondaryControls.push(<FullScreenButton onClick={this.props.onMute} key="fullscreen" />);
        secondaryControls.push(<MuteButton onClick={this.props.onMute} key="mute" />);
        secondaryControls.push(<VideoButton onClick={this.props.onMute} key="video" />);
        secondaryControls.push(<AddUserButton onClick={this.props.onMute} key="add" />);
        mainControls.push(<EndButton onClick={this.props.onEnd} isOutgoing={isOutgoing} key="end" />);
        break;
      case CallStates.ENDED:
        mainControls.push(<CloseButton onClick={this.props.onClose} key="close" />);
        break;
    }

    return (
      <div className="call__controls">
        <div className="call__controls__icons row">
          <div className="col-xs"/>
          {secondaryControls}
          <div className="col-xs"/>
        </div>
        <div className="call__controls__buttons">
          {mainControls}
        </div>
      </div>
    );
  }
}

export default CallControls;
