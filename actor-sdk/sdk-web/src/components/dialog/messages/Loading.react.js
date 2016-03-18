/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import ReactMixin from 'react-mixin';
import PureRenderMixin from 'react-addons-pure-render-mixin';

class Loading extends Component {
  static contextTypes = {
    intl: PropTypes.object
  };

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

ReactMixin.onClass(Loading, PureRenderMixin);

export default Loading;
