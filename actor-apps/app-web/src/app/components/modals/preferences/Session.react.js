/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, { Component } from 'react';
import { Container } from 'flux/utils';
import ReactMixin from 'react-mixin';
import { IntlMixin, FormattedMessage } from 'react-intl';

import PreferencesActionCreators from 'actions/PreferencesActionCreators';

import PreferencesStore from 'stores/PreferencesStore';

import * as Stateful from 'components/common/Stateful.react';

@ReactMixin.decorate(IntlMixin)
class SessionItem extends Component {
  static getStores = () => [PreferencesStore];
  static calculateState = (prevState, props) => {
    return {
      terminateSessionState: PreferencesStore.getTerminateSessionState(props.id)
    }
  };

  static propTypes = {
    appTitle: React.PropTypes.string.isRequired,
    holder: React.PropTypes.string.isRequired,
    id: React.PropTypes.number.isRequired,
    authTime: React.PropTypes.object.isRequired
  };

  onTerminate = () => PreferencesActionCreators.terminateSession(this.props.id);

  render() {
    const { appTitle, holder, authTime } = this.props;
    const { terminateSessionState } = this.state;

    const currentDevice = (holder === 'THIS_DEVICE') ? (
      <small><FormattedMessage message={this.getIntlMessage('preferencesSessionsCurrentSession')}/></small>
    ) : null;

    return (
      <li className="session-list__session">
        <div className="title">
          {appTitle}
          {currentDevice}
        </div>

        <small>
          <b><FormattedMessage message={this.getIntlMessage('preferencesSessionsAuthTime')}/>:</b> {authTime.toString()}
        </small>

        <Stateful.Root currentState={terminateSessionState}>
          <Stateful.Pending>
            <a className="session-list__session__terminate link--blue" onClick={this.onTerminate}>
              <FormattedMessage message={this.getIntlMessage('preferencesSessionsTerminate')}/>
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
