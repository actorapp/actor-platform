/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { Container } from 'flux/utils';

import PreferencesActionCreators from '../../../actions/PreferencesActionCreators';

import PreferencesStore from '../../../stores/PreferencesStore';

import Stateful from '../../common/Stateful';

class SessionItem extends Component {
  constructor(props) {
    super(props);
  }

  static getStores = () => [PreferencesStore];
  static calculateState = (prevState, props) => {
    return {
      terminateSessionState: PreferencesStore.getTerminateSessionState(props.id)
    }
  };

  static propTypes = {
    appTitle: PropTypes.string.isRequired,
    holder: PropTypes.string.isRequired,
    id: PropTypes.number.isRequired,
    authTime: PropTypes.object.isRequired
  };

  static contextTypes = {
    intl: PropTypes.object
  };

  onTerminate = () => PreferencesActionCreators.terminateSession(this.props.id);

  render() {
    const { appTitle, holder, authTime } = this.props;
    const { terminateSessionState } = this.state;
    const { intl } = this.context;

    const currentDevice = (holder === 'THIS_DEVICE') ? (
      <small>{intl.messages['preferencesSessionsCurrentSession']}</small>
    ) : null;

    return (
      <li className="session-list__session">
        <div className="title">
          {appTitle}
          {currentDevice}
        </div>

        <small>
          <b>{intl.messages['preferencesSessionsAuthTime']}:</b> {authTime.toString()}
        </small>

        <Stateful.Root currentState={terminateSessionState}>
          <Stateful.Pending>
            <a className="session-list__session__terminate link--blue" onClick={this.onTerminate}>
              {intl.messages['preferencesSessionsTerminate']}
            </a>
          </Stateful.Pending>
          <Stateful.Processing>
            <i className="session-list__session__terminate material-icons spin">autorenew</i>
          </Stateful.Processing>
          <Stateful.Success>
            <i className="session-list__session__terminate material-icons">check</i>
          </Stateful.Success>
          <Stateful.Failure>
            <i className="session-list__session__terminate material-icons">warning</i>
          </Stateful.Failure>
        </Stateful.Root>
      </li>
    )
  }
}

export default Container.create(SessionItem, {pure: false, withProps: true});
