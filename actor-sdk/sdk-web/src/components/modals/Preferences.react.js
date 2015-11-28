/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { map } from 'lodash';
import React, { Component } from 'react';
import { Container } from 'flux/utils';
import classnames from 'classnames';
import Modal from 'react-modal';
import ReactMixin from 'react-mixin';
import { IntlMixin } from 'react-intl';

import { KeyCodes } from '../../constants/ActorAppConstants';

import PreferencesActionCreators from '../../actions/PreferencesActionCreators';

import PreferencesStore from '../../stores/PreferencesStore';

import Session from './preferences/Session.react'

class PreferencesModal extends Component {
  constructor(props) {
    super(props);
  }

  static getStores = () => [PreferencesStore];

  static calculateState() {
    return {
      isOpen: PreferencesStore.isOpen(),
      isSendByEnterEnabled: PreferencesStore.isSendByEnterEnabled(),
      isSoundEffectsEnabled: PreferencesStore.isSoundEffectsEnabled(),
      isGroupsNotificationsEnabled: PreferencesStore.isGroupsNotificationsEnabled(),
      isOnlyMentionNotifications: PreferencesStore.isOnlyMentionNotifications(),
      isShowNotificationsTextEnabled: PreferencesStore.isShowNotificationsTextEnabled(),
      sessions: PreferencesStore.getSessions(),
      activeTab: PreferencesStore.getCurrentTab()
    }
  };

  componentWillMount() {
    document.addEventListener('keydown', this.onKeyDown, false);
  }

  componentWillUnmount() {
    document.removeEventListener('keydown', this.onKeyDown, false);
  }

  onClose = () => PreferencesActionCreators.hide();

  onDone = () => {
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
    this.onClose();
  };

  onKeyDown = event => {
    if (event.keyCode === KeyCodes.ESC) {
      event.preventDefault();
      this.onClose();
    }
  };

  changeSendByEnter = (event) => this.setState({isSendByEnterEnabled: event.target.value === 'true'});
  changeSoundEffectsEnabled = (event) => this.setState({isSoundEffectsEnabled: event.target.checked});
  changeGroupsNotificationsEnabled = (event) => this.setState({isGroupsNotificationsEnabled: event.target.checked});
  changeMentionNotifications = (event) => this.setState({isOnlyMentionNotifications: event.target.checked});
  changeIsShowNotificationTextEnabled = (event) => this.setState({isShowNotificationsTextEnabled: event.target.checked});

  onTerminateAllSessionsClick = () => PreferencesActionCreators.terminateAllSessions();

  changeTab = (tab) => PreferencesActionCreators.changeTab(tab);

  render() {
    const {
      isOpen,
      activeTab,
      isSendByEnterEnabled,
      isSoundEffectsEnabled,
      isGroupsNotificationsEnabled,
      isOnlyMentionNotifications,
      isShowNotificationsTextEnabled,
      sessions
    } = this.state;

    const sessionList = map(sessions, (session) => <Session {...session}/>);

    const generalTabClassNames = classnames('preferences__tabs__tab', {
      'preferences__tabs__tab--active': activeTab === 'GENERAL'
    });
    const notificationTabClassNames = classnames('preferences__tabs__tab', {
      'preferences__tabs__tab--active': activeTab === 'NOTIFICATIONS'
    });
    const securityTabClassNames = classnames('preferences__tabs__tab', {
      'preferences__tabs__tab--active': activeTab === 'SECURITY'
    });
    const generalTabContentClassName = classnames('preferences__list__item', {
      'preferences__list__item--active': activeTab === 'GENERAL'
    });
    const notificationTabContentClassName = classnames('preferences__list__item', {
      'preferences__list__item--active': activeTab === 'NOTIFICATIONS'
    });
    const securityTabContentClassName = classnames('preferences__list__item', {
      'preferences__list__item--active': activeTab === 'SECURITY'
    });

    return (
      <Modal className="modal-new modal-new--preferences"
             closeTimeoutMS={150}
             isOpen={isOpen}
             style={{width: 760}}>

        <div className="modal-new__header">
          <i className="modal-new__header__icon material-icons">settings</i>
          <h3 className="modal-new__header__title">{this.getIntlMessage('preferencesModalTitle')}</h3>

          <div className="pull-right">
            <button className="button button--lightblue" onClick={this.onDone}>{this.getIntlMessage('button.done')}</button>
          </div>
        </div>

        <div className="modal-new__body">
          <div className="preferences">
            <aside className="preferences__tabs">
              <a className={generalTabClassNames}
                 onClick={() => this.changeTab('GENERAL')}>
                {this.getIntlMessage('preferencesGeneralTab')}
              </a>
              <a className={notificationTabClassNames}
                 onClick={() => this.changeTab('NOTIFICATIONS')}>
                {this.getIntlMessage('preferencesNotificationsTab')}
              </a>
              <a className={securityTabClassNames}
                 onClick={() => this.changeTab('SECURITY')}>
                {this.getIntlMessage('preferencesSecurityTab')}
              </a>
            </aside>
            <div className="preferences__body">
              <div className="preferences__list">
                <div className={generalTabContentClassName}>
                  <ul>
                    <li>
                      <i className="icon material-icons">keyboard</i>
                      <h4>{this.getIntlMessage('preferencesSendMessageTitle')}</h4>
                      <div className="radio">
                        <input type="radio"
                               name="sendByEnter"
                               id="sendByEnterEnabled"
                               value="true"
                               defaultChecked={isSendByEnterEnabled}
                               onChange={this.changeSendByEnter}/>
                        <label htmlFor="sendByEnterEnabled">
                          <b>Enter</b> – {this.getIntlMessage('preferencesSendMessage')}, <b>Shift + Enter</b> – {this.getIntlMessage('preferencesNewLine')}
                        </label>
                      </div>
                      <div className="radio">
                        <input type="radio"
                               name="sendByEnter"
                               id="sendByEnterDisabled"
                               value="false"
                               defaultChecked={!isSendByEnterEnabled}
                               onChange={this.changeSendByEnter}/>
                        <label htmlFor="sendByEnterDisabled">
                          <b>Cmd + Enter</b> – {this.getIntlMessage('preferencesSendMessage')}, <b>Enter</b> – {this.getIntlMessage('preferencesNewLine')}
                        </label>
                      </div>
                    </li>
                  </ul>
                </div>
                <div className={notificationTabContentClassName}>
                  <ul>
                    <li>
                      <i className="icon material-icons">music_note</i>
                      <h4>{this.getIntlMessage('preferencesEffectsTitle')}</h4>
                      <div className="checkbox">
                        <input type="checkbox"
                               id="soundEffects"
                               defaultChecked={isSoundEffectsEnabled}
                               onChange={this.changeSoundEffectsEnabled}/>
                        <label htmlFor="soundEffects">
                          {this.getIntlMessage('preferencesEnableEffects')}
                        </label>
                      </div>
                    </li>
                    <li>
                      <i className="icon material-icons">notifications</i>
                      <h4>{this.getIntlMessage('preferencesNotificationsTitle')}</h4>
                      <div className="checkbox">
                        <input type="checkbox"
                               id="groupNotifications"
                               defaultChecked={isGroupsNotificationsEnabled}
                               onChange={this.changeGroupsNotificationsEnabled}/>
                        <label htmlFor="groupNotifications">
                          {this.getIntlMessage('preferencesNotificationsGroup')}
                        </label>
                      </div>
                      <div className="checkbox">
                        <input type="checkbox"
                               id="mentionsNotifications"
                               defaultChecked={isOnlyMentionNotifications}
                               onChange={this.changeMentionNotifications}/>
                        <label htmlFor="mentionsNotifications">
                          {this.getIntlMessage('preferencesNotificationsOnlyMention')}
                        </label>
                      </div>
                      <p className="hint">{this.getIntlMessage('preferencesNotificationsOnlyMentionHint')}</p>
                    </li>
                    <li>
                      <i className="icon material-icons">visibility</i>
                      <h4>{this.getIntlMessage('preferencesPrivacyTitle')}</h4>
                      <div className="checkbox">
                        <input type="checkbox"
                               id="notificationTextPreview"
                               defaultChecked={isShowNotificationsTextEnabled}
                               onChange={this.changeIsShowNotificationTextEnabled}/>
                        <label htmlFor="notificationTextPreview">
                          {this.getIntlMessage('preferencesMessagePreview')}
                        </label>
                      </div>
                      <p className="hint">{this.getIntlMessage('preferencesMessagePreviewHint')}</p>
                    </li>
                  </ul>
                </div>
                <div className={securityTabContentClassName}>
                  <ul>
                    <li>
                      <i className="icon material-icons">devices</i>
                      <h4>{this.getIntlMessage('preferencesSessionsTitle')}</h4>
                      <ul className="session-list">
                        {sessionList}
                        <li className="session-list__session">
                          <a className="link--red" onClick={this.onTerminateAllSessionsClick}>
                            {this.getIntlMessage('preferencesSessionsTerminateAll')}
                          </a>
                        </li>
                      </ul>
                    </li>
                  </ul>
                </div>

              </div>
            </div>
          </div>
        </div>

      </Modal>
    );
  }
}

ReactMixin.onClass(PreferencesModal, IntlMixin);

export default Container.create(PreferencesModal);
