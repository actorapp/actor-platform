/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component } from 'react';
import { FormattedMessage } from 'react-intl';

import SvgIcon from './common/SvgIcon.react';
import ConnectionState from './common/ConnectionState.react';

class EmptyScreen extends Component {
  render() {
    return (
      <section className="main">
        <div className="flexrow">
          <section className="dialog dialog--empty row center-xs middle-xs">
            <ConnectionState/>

            <div className="advice">
              <div className="logo">
                <SvgIcon className="icon icon--gray" glyph="star" />
              </div>
              <h2><FormattedMessage id="main.empty"/></h2>
            </div>
          </section>
        </div>
      </section>
    );
  }
}

export default EmptyScreen;
