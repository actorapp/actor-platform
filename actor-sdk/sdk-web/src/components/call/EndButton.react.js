/*
* Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
*/

import React, {Component, PropTypes} from 'react';
import { FormattedMessage } from 'react-intl';

class EndButton extends Component {
  static propTypes = {
    onClick: PropTypes.func.isRequired,
    isOutgoing: PropTypes.bool.isRequired
  };

  renderLabel() {
    if (this.props.isOutgoing) {
      return <FormattedMessage id="button.cancel"/>;
    }

    return <FormattedMessage id="call.decline"/>;
  }

  render() {
    return (
      <button className="button button--rised button--wide" onClick={this.props.onClick}>
        {this.renderLabel()}
      </button>
    );
  }
}

export default EndButton;
