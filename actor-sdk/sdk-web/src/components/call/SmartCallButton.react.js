/*
* Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
*/

import React, { Component, PropTypes } from 'react';
import { FormattedMessage } from 'react-intl';
import classnames from 'classnames';

class SmartCallButton extends Component {
  static propTypes = {
    call: PropTypes.object.isRequired,

    onCallStart: PropTypes.func.isRequired,
    onCallEnd: PropTypes.func.isRequired
  };

  constructor(props) {
    super(props);

    this.handleButtonClick = this.handleButtonClick.bind(this);
  }

  handleButtonClick() {
    const { call, onCallStart, onCallEnd } = this.props;

    if (!call.isCalling) {
      onCallStart();
    } else {
      onCallEnd();
    }
  }

  renderButtonIcon() {
    return (
      <i className="material-icons" style={{ fontSize: 22 }}>call</i>
    );
  }

  renderButtonText() {
    const { call } = this.props;

    if (!call.isCalling) {
      return null;
    }

    return (
      <FormattedMessage
        id={`call.state.${call.state}`}
        values={{ time: call.time }}
      />
    );
  }

  render() {
    const { call } = this.props;

    const buttonClassName = classnames('button button--icon call__smart-button', {
      'call__smart-button--in-call': call.isCalling
    })

    return (
      <button className={buttonClassName} onClick={this.handleButtonClick}>
        {this.renderButtonIcon()}
        {this.renderButtonText()}
      </button>
    );
  }
}

export default SmartCallButton;
