/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React from 'react';
import Modal from 'react-modal';
import ReactMixin from 'react-mixin';
import { IntlMixin, FormattedMessage } from 'react-intl';

import { KeyCodes } from 'constants/ActorAppConstants';

import PreferencesActionCreators from 'actions/PreferencesActionCreators';

import PreferencesStore from 'stores/PreferencesStore';

const getStateFromStores = () => {
  return {
    isOpen: PreferencesStore.isModalOpen(),
    isSendByEnterEnabled: PreferencesStore.istSendByEnterEnabled(),
    isSoundEffectsEnabled: PreferencesStore.isSoundEffectsEnabled(),
    isGroupsNotificationsEnabled: PreferencesStore.isGroupsNotificationsEnabled(),
    isOnlyMentionNotifications: PreferencesStore.isOnlyMentionNotifications()
  };
};

@ReactMixin.decorate(IntlMixin)
class PreferencesModal extends React.Component {
  constructor(props) {
    super(props);

    this.state = getStateFromStores();

    PreferencesStore.addChangeListener(this.onChange);
    document.addEventListener('keydown', this.onKeyDown, false);
  }

  componentWillUnmount() {
    PreferencesStore.removeChangeListener(this.onChange);
    document.removeEventListener('keydown', this.onKeyDown, false);
  }

  onChange = () => {
    this.setState(getStateFromStores());
  };

  onClose = () => {
    PreferencesActionCreators.hide();
  };

  onDone = () => {
    const {
      isSendByEnterEnabled,
      isSoundEffectsEnabled,
      isGroupsNotificationsEnabled,
      isOnlyMentionNotifications
      } = this.state;

    PreferencesActionCreators.save({
      isSendByEnterEnabled, isSoundEffectsEnabled, isGroupsNotificationsEnabled, isOnlyMentionNotifications
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

  render() {
    const {
      isOpen,
      isSendByEnterEnabled,
      isSoundEffectsEnabled,
      isGroupsNotificationsEnabled,
      isOnlyMentionNotifications
      } = this.state;

    if (isOpen) {
      return (
        <Modal className="modal-new modal-new--preferences"
               closeTimeoutMS={150}
               isOpen={isOpen}
               style={{width: 560}}>

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
                        <div className="radio">
                          <input type="radio"
                                 name="sendByEnter"
                                 id="sendByEnterEnabled"
                                 value={true}
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
                                 value={false}
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
                        <div className="checkbox">
                          <input type="checkbox"
                                 id="groupNotifications"
                                 defaultChecked={isGroupsNotificationsEnabled}
                                 onChange={this.changeGroupsNotificationsEnabled}/>
                          <label htmlFor="groupNotifications">Enable group notifications</label>
                        </div>
                      </li>
                      <li>
                        <div className="checkbox">
                          <input type="checkbox"
                                 id="mentionsNotifications"
                                 defaultChecked={isOnlyMentionNotifications}
                                 onChange={this.changeMentionNotifications}/>
                          <label htmlFor="mentionsNotifications">Enable mention notifications</label>
                        </div>
                      </li>
                    </ul>
                  </div>
                  <div className="preferences__list__item preferences__list__item--notifications">
                    <ul>
                      <li>
                        <i className="icon material-icons">music_note</i>
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

export default PreferencesModal;
