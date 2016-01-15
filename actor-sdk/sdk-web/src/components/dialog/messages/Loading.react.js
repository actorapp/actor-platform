/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import ReactMixin from 'react-mixin';
import { IntlMixin } from 'react-intl';
import addons from 'react/addons';
const {addons: { PureRenderMixin }} = addons;

class Loading extends Component {
  constructor(props) {
    super(props);
  }

  render() {
    return(
      <li className="message message--loading">
        <div className="message__body col-xs text-center">
          {this.getIntlMessage('message.loading')}
        </div>
      </li>
    )
  }
}

ReactMixin.onClass(Loading, IntlMixin);
ReactMixin.onClass(Loading, PureRenderMixin);

export default Loading;
