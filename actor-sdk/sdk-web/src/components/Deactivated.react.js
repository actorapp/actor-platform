/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component } from 'react';
import { FormattedMessage } from 'react-intl';
import SharedContainer from '../utils/SharedContainer';
import { appName } from '../constants/ActorAppConstants';

class Deactivated extends Component {
  constructor(props){
    super(props);

    const SharedActor = SharedContainer.get();
    this.appName = SharedActor.appName ? SharedActor.appName : appName;
  }

  render() {
    return (
      <div className="deactivated row center-xs middle-xs">
        <div className="deactivated__window">
          <h2><FormattedMessage id="main.deactivated.header"/></h2>
          <p><FormattedMessage id="main.deactivated.text" values={{ appName: this.appName }}/></p>
        </div>
      </div>
    );
  }
}

export default Deactivated;
