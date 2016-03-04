/*
* Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
*/

import React, {Component, PropTypes} from 'react';
import { FormattedMessage } from 'react-intl';

class MuteButton extends Component {
  static propTypes = {
    value: PropTypes.bool.isRequired,
    onToggle: PropTypes.func.isRequired
  };

  render() {
    const glyph = this.props.value ? 'mic_off' : 'mic';

    return (
      <button className="button button--square col-xs" onClick={this.props.onToggle}>
        <i className="material-icons">{glyph}</i>
        <FormattedMessage id="call.mute"/>
      </button>
    );
  }
}

export default MuteButton;
