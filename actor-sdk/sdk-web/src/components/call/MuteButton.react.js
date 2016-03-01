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
      <button className="button button--rised button--wide" onClick={this.props.onClick}>
        <FormattedMessage id="call.mute"/>
      </button>
    );
  }
}

export default MuteButton;
