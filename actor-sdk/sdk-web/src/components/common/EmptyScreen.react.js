/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import ReactMixin from 'react-mixin';
import { IntlMixin } from 'react-intl';

import ConnectionState from './ConnectionState.react';

class EmptyScreen extends Component {
  constructor(props) {
    super(props);
  }

  render() {
    return (
      <section className="dialog dialog--empty row center-xs middle-xs">
        <ConnectionState/>

        <div className="advice">
          <div className="logo">
            <svg className="icon icon--gray"
                 dangerouslySetInnerHTML={{__html: '<use xlink:href="assets/images/icons.svg#star"/>'}}/>
          </div>
          <h2>{this.getIntlMessage('main.empty')}</h2>
        </div>
      </section>
    );
  }
}

ReactMixin.onClass(EmptyScreen, IntlMixin);

export default EmptyScreen;
