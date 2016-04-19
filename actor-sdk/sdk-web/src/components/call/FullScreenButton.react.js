/*
* Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
*/

import React, { Component, PropTypes } from 'react';
import { FormattedMessage } from 'react-intl';

class FullScreenButton extends Component {
  static propTypes = {
    onClick: PropTypes.func.isRequired
  };

  render() {
    return (
      <button className="button button--square col-xs" disabled onClick={this.props.onClick}>
        <i className="material-icons">fullscreen</i>
        <FormattedMessage id="call.fullScreen"/>
      </button>
    );
  }
}

export default FullScreenButton;
