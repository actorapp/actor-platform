/*
 * Copyright (C) 2016 Actor LLC. <https://actor.im>
 */

import React, { Component } from 'react';
import { Container } from 'flux/utils';
import classnames from 'classnames';
import Modal from 'react-modal';
import { FormattedMessage } from 'react-intl';
import SharedContainer from '../../utils/SharedContainer'
import { appName, PreferencesTabTypes, AsyncActionStates } from '../../constants/ActorAppConstants';

import PreferencesActionCreators from '../../actions/PreferencesActionCreators';
import { loggerToggle } from '../../actions/LoggerActionCreators';

import PreferencesStore from '../../stores/PreferencesStore';

import Session from './preferences/Session.react'

class PreferencesModal extends Component {
  static getStores() {
    return [PreferencesStore];
  }

  static calculateState(prevState) {
    return {
      isSendByEnterEnabled: prevState ? prevState.isSendByEnterEnabled : PreferencesStore.isSendByEnterEnabled(),
      isSoundEffectsEnabled: prevState ? prevState.isSoundEffectsEnabled : PreferencesStore.isSoundEffectsEnabled(),
      isGroupsNotificationsEnabled: prevState ? prevState.isGroupsNotificationsEnabled : PreferencesStore.isGroupsNotificationsEnabled(),
      isOnlyMentionNotifications: prevState ? prevState.isOnlyMentionNotifications : PreferencesStore.isOnlyMentionNotifications(),
      isShowNotificationsTextEnabled: prevState ? prevState.isShowNotificationsTextEnabled : PreferencesStore.isShowNotificationsTextEnabled(),
      sessions: PreferencesStore.getSessions(),
      activeTab: PreferencesStore.getCurrentTab(),
      terminateState: PreferencesStore.getTerminateState()
    }
  }

  constructor(props) {
    super(props);

    const SharedActor = SharedContainer.get();
    this.appName = SharedActor.appName ? SharedActor.appName : appName;
    this.loggerToggleCount = 0;

    this.handleClose = this.handleClose.bind(this);
    this.handleSave = this.handleSave.bind(this);
    this.handleChangeTab = this.handleChangeTab.bind(this);
    this.handleAppDetailClick = this.handleAppDetailClick.bind(this);
    this.toggleSendByEnter = this.toggleSendByEnter.bind(this);
    this.changeSoundEffectsEnabled = this.changeSoundEffectsEnabled.bind(this);
    this.changeGroupsNotificationsEnabled = this.changeGroupsNotificationsEnabled.bind(this);
    this.changeMentionNotifications = this.changeMentionNotifications.bind(this);
    this.changeIsShowNotificationTextEnabled = this.changeIsShowNotificationTextEnabled.bind(this);
    this.handleTerminateAllSessionsClick = this.handleTerminateAllSessionsClick.bind(this);
  }

  handleAppDetailClick() {
    this.loggerToggleCount++;
    if (this.loggerToggleCount >= 4) {
      loggerToggle();
      this.loggerToggleCount = 0;
    }
  }

  handleClose() {
    PreferencesActionCreators.hide();
  }

  handleSave() {
    const {
      isSendByEnterEnabled,
      isSoundEffectsEnabled,
      isGroupsNotificationsEnabled,
      isOnlyMentionNotifications,
      isShowNotificationsTextEnabled
    } = this.state;

    PreferencesActionCreators.save({
      isSendByEnterEnabled,
      isSoundEffectsEnabled,
      isGroupsNotificationsEnabled,
      isOnlyMentionNotifications,
      isShowNotificationsTextEnabled
    });
    this.handleClose();
  }

  toggleSendByEnter(event) {
    this.setState({isSendByEnterEnabled: event.target.value === 'true'});
  }

  changeSoundEffectsEnabled(event) {
    this.setState({isSoundEffectsEnabled: event.target.checked});
  }

  changeGroupsNotificationsEnabled(event) {
    this.setState({isGroupsNotificationsEnabled: event.target.checked});
  }

  changeMentionNotifications(event) {
    this.setState({isOnlyMentionNotifications: event.target.checked});
  }

  changeIsShowNotificationTextEnabled(event) {
    this.setState({isShowNotificationsTextEnabled: event.target.checked});
  }

  handleTerminateAllSessionsClick() {
    PreferencesActionCreators.terminateAllSessions();
  }

  handleChangeTab(tab) {
    PreferencesActionCreators.changeTab(tab);
  }

  renderAppDetail() {
    return (
      <span onClick={this.handleAppDetailClick}>
        {`${this.appName}: v${__ACTOR_SDK_VERSION__}`}
        <br/>
        {`Core: v${__ACTOR_CORE_VERSION__}`}
      </span>
    );
  }

  renderPreferencesSidebar() {
    const { activeTab } = this.state;

    const generalTabClassNames = classnames('preferences__tabs__tab', {
      'preferences__tabs__tab--active': activeTab === PreferencesTabTypes.GENERAL
    });
    const notificationTabClassNames = classnames('preferences__tabs__tab', {
      'preferences__tabs__tab--active': activeTab ===  PreferencesTabTypes.NOTIFICATIONS
    });
    const securityTabClassNames = classnames('preferences__tabs__tab', {
      'preferences__tabs__tab--active': activeTab ===  PreferencesTabTypes.SECURITY
    });

    return (
      <aside className="preferences__tabs">
        <a className={generalTabClassNames} onClick={() => this.handleChangeTab('GENERAL')}>
          <FormattedMessage id="preferences.general.title"/>
        </a>
        <a className={notificationTabClassNames} onClick={() => this.handleChangeTab('NOTIFICATIONS')}>
          <FormattedMessage id="preferences.notifications.title"/>
        </a>
        <a className={securityTabClassNames} onClick={() => this.handleChangeTab('SECURITY')}>
          <FormattedMessage id="preferences.security.title"/>
        </a>
        <footer className="preferences__tabs__footer">
          {this.renderAppDetail()}
        </footer>
      </aside>
    );
  }

  renderGeneralTab() {
    const { isSendByEnterEnabled } = this.state;

    return (
      <div className="preferences__tabs__content">
        <ul>
          <li>
            <i className="icon material-icons">keyboard</i>
            <FormattedMessage id="preferences.general.send.title" tagName="h4"/>
            <div className="radio">
              <input
                type="radio"
                name="sendByEnter"
                id="sendByEnterEnabled"
                value="true"
                defaultChecked={isSendByEnterEnabled}
                onChange={this.toggleSendByEnter}/>
              <label htmlFor="sendByEnterEnabled">
                <b>Enter</b> – <FormattedMessage id="preferences.general.send.sendMessage"/>,
                <b>Shift + Enter</b> – <FormattedMessage id="preferences.general.send.newLine"/>
              </label>
            </div>
            <div className="radio">
              <input
                type="radio"
                name="sendByEnter"
                id="sendByEnterDisabled"
                value="false"
                defaultChecked={!isSendByEnterEnabled}
                onChange={this.toggleSendByEnter}/>
              <label htmlFor="sendByEnterDisabled">
                <b>Cmd + Enter</b> – <FormattedMessage id="preferences.general.send.sendMessage"/>,
                <b>Enter</b> – <FormattedMessage id="preferences.general.send.newLine"/>
              </label>
            </div>
          </li>
        </ul>
      </div>
    );
  }

  renderNotificationsTab() {
    const {
      isSoundEffectsEnabled,
      isGroupsNotificationsEnabled,
      isOnlyMentionNotifications,
      isShowNotificationsTextEnabled
    } = this.state;

    return (
      <div className="preferences__tabs__content">
        <ul>
          <li>
            <i className="icon material-icons">music_note</i>
            <FormattedMessage id="preferences.notifications.effects.title" tagName="h4"/>
            <div className="checkbox">
              <input
                type="checkbox"
                id="soundEffects"
                defaultChecked={isSoundEffectsEnabled}
                onChange={this.changeSoundEffectsEnabled}/>
              <label htmlFor="soundEffects">
                <FormattedMessage id="preferences.notifications.effects.enable"/>
              </label>
            </div>
          </li>
          <li>
            <i className="icon material-icons">notifications</i>
            <FormattedMessage id="preferences.notifications.notification.title" tagName="h4"/>
            <div className="checkbox">
              <input
                type="checkbox"
                id="groupNotifications"
                defaultChecked={isGroupsNotificationsEnabled}
                onChange={this.changeGroupsNotificationsEnabled}/>
              <label htmlFor="groupNotifications">
                <FormattedMessage id="preferences.notifications.notification.enable"/>
              </label>
            </div>
            <div className="checkbox">
              <input
                type="checkbox"
                id="mentionsNotifications"
                defaultChecked={isOnlyMentionNotifications}
                onChange={this.changeMentionNotifications}/>
              <label htmlFor="mentionsNotifications">
                <FormattedMessage id="preferences.notifications.notification.onlyMentionEnable"/>
              </label>
            </div>
            <p className="hint"><FormattedMessage id="preferences.notifications.notification.onlyMentionHint"/></p>
          </li>
          <li>
            <i className="icon material-icons">visibility</i>
            <FormattedMessage id="preferences.notifications.privacy.title" tagName="h4"/>
            <div className="checkbox">
              <input
                type="checkbox"
                id="notificationTextPreview"
                defaultChecked={isShowNotificationsTextEnabled}
                onChange={this.changeIsShowNotificationTextEnabled}/>
              <label htmlFor="notificationTextPreview">
                <FormattedMessage id="preferences.notifications.privacy.messagePreview"/>
              </label>
            </div>
            <p className="hint"><FormattedMessage id="preferences.notifications.privacy.messagePreviewHint"/></p>
          </li>
        </ul>
      </div>
    );
  }

  renderSecurityTab() {
    return (
      <div className="preferences__tabs__content">
        <ul>
          <li>
            <i className="icon material-icons">devices</i>
            <FormattedMessage id="preferences.security.sessions.title" tagName="h4"/>
            <ul className="session-list">
              {this.renderSessionList()}
              <li className="session-list__session">
                <a className="link--red" onClick={this.handleTerminateAllSessionsClick}>
                  <FormattedMessage id="preferences.security.sessions.terminateAll"/>
                </a>
              </li>
            </ul>
          </li>
        </ul>
      </div>
    );
  }

  renderSessionList() {
    const { sessions, terminateState } = this.state;
    return sessions.map((session, index) => {
      return (
        <Session
          {...session}
          key={`s${index}`}
          terminateState={terminateState[session.id] || AsyncActionStates.PENDING}
        />
      );
    });
  }

  renderCurrentTab() {
    const { activeTab } = this.state;
    switch (activeTab) {
      case PreferencesTabTypes.GENERAL:
        return this.renderGeneralTab()
      case PreferencesTabTypes.NOTIFICATIONS:
        return this.renderNotificationsTab()
      case PreferencesTabTypes.SECURITY:
        return this.renderSecurityTab()
      default:
        return null;
    }
  }

  render() {
    return (
      <Modal
        overlayClassName="modal-overlay"
        className="modal"
        onRequestClose={this.handleClose}
        isOpen>

        <div className="preferences">
          <div className="modal__content">

            <header className="modal__header">
              <i className="modal__header__icon material-icons">settings</i>
              <FormattedMessage id="preferences.title" tagName="h1"/>
              <button className="button button--lightblue" onClick={this.handleSave}>
                <FormattedMessage id="button.done"/>
              </button>
            </header>

            <div className="modal__body">
              {this.renderPreferencesSidebar()}
              <div className="preferences__body">
                {this.renderCurrentTab()}
              </div>
            </div>

          </div>
        </div>

      </Modal>
    );
  }
}

export default Container.create(PreferencesModal, {pure: false});
