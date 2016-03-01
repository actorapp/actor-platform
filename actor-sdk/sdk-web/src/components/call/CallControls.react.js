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

    const controls = [];
    switch (this.props.callState) {
      case CallStates.CALLING:
        if (!isOutgoing) {
          controls.push(<AnswerButton onClick={this.props.onAnswer} key="answer" />)
        }

        controls.push(<EndButton onClick={this.props.onEnd} isOutgoing={isOutgoing} key="end" />);
        break;
      case CallStates.IN_PROGRESS:
      case CallStates.CONNECTING:
        controls.push(<MuteButton onClick={this.props.onMute} key="mute" />)
        controls.push(<EndButton onClick={this.props.onEnd} isOutgoing={isOutgoing} key="end" />);
        break;
      case CallStates.ENDED:
        controls.push(<CloseButton onClick={this.props.onClose} key="close" />);
        break;
    }

    return (
      <div>
        {controls}
      </div>
    );
  }
}

export default CallControls;
