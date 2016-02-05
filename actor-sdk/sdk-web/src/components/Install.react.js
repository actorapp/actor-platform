/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component } from 'react';
import ReactMixin from 'react-mixin';
import { IntlMixin, FormattedHTMLMessage } from 'react-intl';
import SharedContainer from '../utils/SharedContainer';
import { appName } from '../constants/ActorAppConstants';

class Install extends Component {
  constructor(props){
    super(props);

    const SharedActor = SharedContainer.get();
    this.appName = SharedActor.appName ? SharedActor.appName : appName;
  }

  render() {
    return (
      <section className="mobile-placeholder col-xs row center-xs middle-xs">
        <div>
          <img alt={`${this.appName} messenger`}
               className="logo"
               src="assets/images/logo.png"
               srcSet="assets/images/logo@2x.png 2x"/>

          <FormattedHTMLMessage message={this.getIntlMessage('main.install')} appName={this.appName}/>

          <p>
            <a href="//actor.im/ios">iPhone</a> | <a href="//actor.im/android">Android</a>
          </p>
        </div>
      </section>
    );
  }
}

ReactMixin.onClass(Install, IntlMixin);

export default Install;
