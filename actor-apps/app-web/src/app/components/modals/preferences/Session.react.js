/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, { Component } from 'react';
import { Container } from 'flux/utils';

import PreferencesActionCreators from 'actions/PreferencesActionCreators';

import PreferencesStore from 'stores/PreferencesStore';

class SessionItem extends Component {
  static getStores = () => [PreferencesStore];
  static calculateState = () => {};

  static propTypes = {
    appTitle: React.PropTypes.string.isRequired,
    holder: React.PropTypes.string.isRequired,
    id: React.PropTypes.number.isRequired,
    authTime: React.PropTypes.object.isRequired
  };

  onTerminate = () => PreferencesActionCreators.terminateSession(this.props.id);

  render() {
    const { appTitle, holder, authTime } = this.props;
    const currentDevice = (holder === 'THIS_DEVICE') ? <small>Current session</small> : null;

    return (
      <li className="session-list__session">
        <div className="title">
          {appTitle}
          {currentDevice}
        </div>
        <small><b>Auth time:</b> {authTime.toString()}</small>
        <a className="session-list__session__terminate link--blue" onClick={this.onTerminate}>Kill</a>
      </li>
    )
  }
}

export default Container.create(SessionItem);
