/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component } from 'react';
import { FormattedMessage } from 'react-intl';

class Loading extends Component {
  shouldComponentUpdate() {
    return false;
  }

  render() {
    return(
      <div className="message message--loading">
        <div className="message__body col-xs text-center">
          <FormattedMessage id="message.loading"/>
        </div>
      </div>
    )
  }
}

export default Loading;
