/*
* Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
*/

import React, { Component, PropTypes } from 'react';
import Draggable from 'react-draggable';

import CallBody from './CallBody.react';
import CallControls from './CallControls.react';

class CallDraggable extends Component {
  static propTypes = {
    peerInfo: React.PropTypes.object,
    callState: PropTypes.string.isRequired,
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
    const { peerInfo, callState } = this.props;

    return (
      <Draggable>
        <section className="call__draggable" style={{ position: 'absolute', top: 140, right: 32 }}>
          <CallBody peerInfo={peerInfo} callState={callState} small />
          <CallControls
            callState={callState}
            isOutgoing={this.props.isOutgoing}
            isMuted={this.props.isMuted}
            onEnd={this.props.onEnd}
            onAnswer={this.props.onAnswer}
            onMuteToggle={this.props.onMuteToggle}
            onFullscreen={this.props.onFullscreen}
            onUserAdd={this.props.onUserAdd}
            onVideo={this.props.onVideo}
            onClose={this.props.onClose}
            small
          />
        </section>
      </Draggable>
    );
  }
}

export default CallDraggable;
