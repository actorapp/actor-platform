/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { shouldComponentUpdate } from 'react-addons-pure-render-mixin';

class Loading extends Component {
  static contextTypes = {
    intl: PropTypes.object
  };

  constructor(props) {
    super(props);

    this.shouldComponentUpdate = shouldComponentUpdate.bind(this);
  }

  render() {
    const { intl } = this.context;

    return(
      <div className="message message--loading">
        <div className="message__body col-xs text-center">
          {intl.messages['message.loading']}
        </div>
      </div>
    )
  }
}

export default Loading;
