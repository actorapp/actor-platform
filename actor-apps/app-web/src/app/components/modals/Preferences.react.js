/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { map } from 'lodash';
import React, { Component } from 'react';
import { Container } from 'flux/utils';
import Modal from 'react-modal';
import ReactMixin from 'react-mixin';
import { IntlMixin, FormattedMessage } from 'react-intl';

import { KeyCodes } from 'constants/ActorAppConstants';

import PreferencesActionCreators from 'actions/PreferencesActionCreators';

import PreferencesStore from 'stores/PreferencesStore';

import Session from './preferences/Session.react'

@ReactMixin.decorate(IntlMixin)
class PreferencesModal extends Component {
  static getStores = () => [PreferencesStore];

  static calculateState() {
    return {
      isOpen: PreferencesStore.isOpen(),
      isSendByEnterEnabled: PreferencesStore.isSendByEnterEnabled(),
      isSoundEffectsEnabled: PreferencesStore.isSoundEffectsEnabled(),
      isGroupsNotificationsEnabled: PreferencesStore.isGroupsNotificationsEnabled(),
      isOnlyMentionNotifications: PreferencesStore.isOnlyMentionNotifications(),
      isNotificationTextPreviewEnabled: PreferencesStore.isNotificationTextPreviewEnabled(),
      sessions: PreferencesStore.getSessions()
    }
  };

  componentWillUpdate(nextProps, nextState) {
    const { isOpen } = nextState;

    if (isOpen) {
      document.addEventListener('keydown', this.onKeyDown, false);
    } else {
      document.removeEventListener('keydown', this.onKeyDown, false);
    }
  }

  onClose = () => PreferencesActionCreators.hide();

  onDone = () => {
    const {
      isSendByEnterEnabled,
      isSoundEffectsEnabled,
      isGroupsNotificationsEnabled,
      isOnlyMentionNotifications,
      isNotificationTextPreviewEnabled
    } = this.state;

    PreferencesActionCreators.save({
      isSendByEnterEnabled, isSoundEffectsEnabled, isGroupsNotificationsEnabled, isOnlyMentionNotifications, isNotificationTextPreviewEnabled
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
  changeNotificationTextPreviewEnabled = (event) => this.setState({isOnlyMentionNotifications: event.target.checked});

  onTerminateAllSessionsClick = () => PreferencesActionCreators.terminateAllSessions();

  render() {
    const {
      isOpen,
      isSendByEnterEnabled,
      isSoundEffectsEnabled,
      isGroupsNotificationsEnabled,
      isOnlyMentionNotifications,
      sessions,
      isNotificationTextPreviewEnabled
    } = this.state;

    const sessionList = map(sessions, (session) => <Session {...session}/>);

    if (isOpen) {
      return (
        <Modal className="modal-new modal-new--preferences"
               closeTimeoutMS={150}
               isOpen={isOpen}
               style={{width: 500}}>

          <div className="modal-new__header">
            <i className="modal-new__header__icon material-icons">settings</i>

            <h3 className="modal-new__header__title">
              <FormattedMessage message={this.getIntlMessage('preferencesModalTitle')}/>
            </h3>

            <div className="pull-right">
              <button className="button button--lightblue" onClick={this.onDone}>Done</button>
            </div>
          </div>

          <div className="modal-new__body">
            <div className="preferences">
              <div className="preferences__body">
                <div className="preferences__list">
                  <div className="preferences__list__item  preferences__list__item--general">
                    <ul>
                      <li>
                        <i className="icon material-icons">keyboard</i>
                        <h4>Send message</h4>
                        <div className="radio">
                          <input type="radio"
                                 name="sendByEnter"
                                 id="sendByEnterEnabled"
                                 value="true"
                                 defaultChecked={isSendByEnterEnabled}
                                 onChange={this.changeSendByEnter}/>
                          <label htmlFor="sendByEnterEnabled">
                            <b>Enter</b> – send message, <b>Shift + Enter</b> – new line
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
                            <b>Cmd + Enter</b> – send message, <b>Enter</b> – new line
                          </label>
                        </div>
                      </li>
                    </ul>
                  </div>
                  <div className="preferences__list__item preferences__list__item--notifications">
                    <ul>
                      <li>
                        <i className="icon material-icons">notifications</i>
                        <h4>Notifications</h4>
                        <div className="checkbox">
                          <input type="checkbox"
                                 id="groupNotifications"
                                 defaultChecked={isGroupsNotificationsEnabled}
                                 onChange={this.changeGroupsNotificationsEnabled}/>
                          <label htmlFor="groupNotifications">Enable group notifications</label>
                        </div>
                        <div className="checkbox">
                          <input type="checkbox"
                                 id="mentionsNotifications"
                                 defaultChecked={isOnlyMentionNotifications}
                                 onChange={this.changeMentionNotifications}/>
                          <label htmlFor="mentionsNotifications">Enable mention only notifications</label>
                        </div>
                      </li>
                    </ul>
                  </div>
                  <div className="preferences__list__item preferences__list__item--effects">
                    <ul>
                      <li>
                        <i className="icon material-icons">music_note</i>
                        <h4>Sound effects</h4>
                        <div className="checkbox">
                          <input type="checkbox"
                                 id="soundEffects"
                                 defaultChecked={isSoundEffectsEnabled}
                                 onChange={this.changeSoundEffectsEnabled}/>
                          <label htmlFor="soundEffects">Enable sound effects</label>
                        </div>
                      </li>
                    </ul>
                  </div>
                  <div className="preferences__list__item preferences__list__item--effects">
                    <ul>
                      <li>
                        <i className="icon material-icons">view_headline</i>
                        <h4>Messages preview</h4>
                        <div className="checkbox">
                          <input type="checkbox"
                                 id="notificationTextPreview"
                                 defaultChecked={isNotificationTextPreviewEnabled}
                                 onChange={this.changeNotificationTextPreviewEnabled}/>
                          <label htmlFor="notificationTextPreview">Preview text message in notifications</label>
                        </div>
                      </li>
                    </ul>
                  </div>
                  <div className="preferences__list__item preferences__list__item--sessions">
                    <ul>
                      <li>
                        <i className="icon material-icons">devices</i>
                        <h4>Active sessions</h4>
                        <ul className="session-list">
                          {sessionList}
                          <li className="session-list__session">
                            <a className="link--red" onClick={this.onTerminateAllSessionsClick}>Terminate all sessions</a>
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
    } else {
      return null;
    }
  }
}

export default Container.create(PreferencesModal, {pure: false});
