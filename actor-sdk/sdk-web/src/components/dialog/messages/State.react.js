/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { MessageContentTypes, MessageStates } from '../../../constants/ActorAppConstants';

class State extends Component {
  static propTypes = {
    message: PropTypes.shape({
      state: PropTypes.oneOf([
        MessageStates.PENDING,
        MessageStates.SENT,
        MessageStates.RECEIVED,
        MessageStates.READ,
        MessageStates.ERROR,
        MessageStates.UNKNOWN
      ]).isRequired
    }).isRequired
  };

  renderState() {
    const {state} = this.props.message;

    switch (state) {
      case MessageStates.PENDING:
        return <i className="status status--pending material-icons icon-access_time"></i>;
      case MessageStates.SENT:
        return <i className="status status--sent material-icons icon-done"></i>;
      case MessageStates.RECEIVED:
        return <i className="status status--received material-icons icon-done_all"></i>;
      case MessageStates.READ:
        return <i className="status status--read material-icons icon-done_all"></i>;
      case MessageStates.ERROR:
        return <i className="status status--error material-icons icon-report_problem"></i>;
      case MessageStates.UNKNOWN:
      default:
        return null;
    }
  }

  render() {
    const { message } = this.props;

    if (message.content.content === MessageContentTypes.SERVICE) {
      return null;
    }

    const state = this.renderState();
    if (!state) {
      return null;
    }

    return (
      <div className="message__status">
        {state}
      </div>
    );
  }
}

export default State;
