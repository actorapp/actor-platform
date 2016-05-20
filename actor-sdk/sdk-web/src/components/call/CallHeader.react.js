/*
* Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
*/

import React, { Component, PropTypes } from 'react';
import { FormattedMessage } from 'react-intl';

class CallHeader extends Component {
  static propTypes = {
    isOutgoing: PropTypes.bool.isRequired
  };

  renderLabel() {
    if (this.props.isOutgoing) {
      return <FormattedMessage id="call.outgoing" />;
    }

    return <FormattedMessage id="call.incoming" />;
  }

  render() {
    return (
      <header className="call__header">
        <h2>
          {this.renderLabel()}
        </h2>
      </header>
    );
  }
}

export default CallHeader;
