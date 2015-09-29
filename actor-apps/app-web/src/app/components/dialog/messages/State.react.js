/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, { Component } from 'react';
import { MessageContentTypes } from 'constants/ActorAppConstants';

export default class State extends Component {
  static propTypes = {
    message: React.PropTypes.object.isRequired
  };

  render() {
    const { message } = this.props;

    if (message.content.content === MessageContentTypes.SERVICE) {
      return null;
    } else {
      let icon = null;

      switch (message.state) {
        case 'pending':
          icon = <i className="status status--pending material-icons">access_time</i>;
          break;
        case 'sent':
          icon = <i className="status status--sent material-icons">done</i>;
          break;
        case 'received':
          icon = <i className="status status--received material-icons">done_all</i>;
          break;
        case 'read':
          icon = <i className="status status--read material-icons">done_all</i>;
          break;
        case 'error':
          icon = <i className="status status--error material-icons">report_problem</i>;
          break;
        default:
      }

      return (
        <div className="message__status">{icon}</div>
      );
    }
  }
}
