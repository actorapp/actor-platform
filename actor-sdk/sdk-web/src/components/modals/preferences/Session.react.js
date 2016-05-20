/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { FormattedMessage } from 'react-intl';
import { AsyncActionStates } from '../../../constants/ActorAppConstants';

import PreferencesActionCreators from '../../../actions/PreferencesActionCreators';

import Stateful from '../../common/Stateful.react';

class SessionItem extends Component {
  static propTypes = {
    appTitle: PropTypes.string.isRequired,
    holder: PropTypes.string.isRequired,
    id: PropTypes.number.isRequired,
    authTime: PropTypes.object.isRequired,
    terminateState: PropTypes.oneOf([
      AsyncActionStates.PENDING,
      AsyncActionStates.PROCESSING,
      AsyncActionStates.SUCCESS,
      AsyncActionStates.FAILURE
    ]).isRequired
  };

  constructor(props) {
    super(props);

    this.handleTerminateSession = this.handleTerminateSession.bind(this);
  }

  handleTerminateSession() {
    PreferencesActionCreators.terminateSession(this.props.id);
  }

  renderTitle() {
    const { appTitle } = this.props;

    return (
      <div className="title">
        {appTitle}
        {this.renderCurrentDeviceMark()}
      </div>
    );
  }

  renderCurrentDeviceMark() {
    const { holder } = this.props;
    if (holder !== 'THIS_DEVICE') return null;

    return (
      <FormattedMessage id="preferences.security.sessions.current" tagName="small"/>
    );
  }

  renderAuthTime() {
    const { authTime } = this.props;

    return (
      <small>
        <b><FormattedMessage id="preferences.security.sessions.authTime"/>:</b> {authTime.toString()}
      </small>
    );
  }

  renderState() {
    const { terminateState } = this.props;

    return (
      <Stateful
        currentState={terminateState}
        pending={
          <a className="session-list__session__terminate link--blue" onClick={this.handleTerminateSession}>
            <FormattedMessage id="preferences.security.sessions.terminate"/>
          </a>
        }
        processing={
          <i className="session-list__session__terminate material-icons spin">autorenew</i>
        }
        success={
          <i className="session-list__session__terminate material-icons">check</i>
        }
        failure={
          <i className="session-list__session__terminate material-icons">warning</i>
        }
      />
    );
  }

  render() {
    return (
      <li className="session-list__session">
        {this.renderTitle()}
        {this.renderAuthTime()}
        {this.renderState()}
      </li>
    )
  }
}

export default SessionItem;
