/*
* Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
*/

import React, {Component, PropTypes} from 'react';
import { FormattedMessage } from 'react-intl';

class MuteButton extends Component {
  static propTypes = {
    onClick: PropTypes.func.isRequired
  };

  render() {
    return (
      <button className="button button--square col-xs" onClick={this.props.onClick}>
        <i className="material-icons">mic_off</i>
        <FormattedMessage id="call.mute"/>
      </button>
    );
  }
}

export default MuteButton;
