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
    isSendByEnterEnabled: PreferencesStore.getSendByEnter(),
    isSoundEffectsEnabled: PreferencesStore.getSoundEffectsEnabled()
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
    const { isSendByEnterEnabled, isSoundEffectsEnabled } = this.state;
    PreferencesActionCreators.save({
      isSendByEnterEnabled, isSoundEffectsEnabled
    });
    this.onClose();
  };

  onKeyDown = event => {
    if (event.keyCode === KeyCodes.ESC) {
      event.preventDefault();
      this.onClose();
    }
  };

  changeSendByEnter = (event) => this.setState({isSendByEnterEnabled: event.target.value});
  changeSoundEffectsEnabled = (event) => this.setState({isSoundEffectsEnabled: event.target.checked});

  render() {
    const { isOpen, isSendByEnterEnabled, isSoundEffectsEnabled } = this.state;

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
                          <label htmlFor="sendByEnterEnabled"><b>Enter</b> – send message, <b>Shift + Enter</b> – new line</label>
                        </div>
                        <div className="radio">
                          <input type="radio"
                                 name="sendByEnter"
                                 id="sendByEnterDisabled"
                                 value={false}
                                 defaultChecked={!isSendByEnterEnabled}
                                 onChange={this.changeSendByEnter}/>
                          <label htmlFor="sendByEnterDisabled"><b>Cmd + Enter</b> – send message, <b>Enter</b> – new line</label>
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
                                 id="soundEffects"
                                 defaultChecked={isSoundEffectsEnabled}
                                 onChange={this.changeSoundEffectsEnabled}/>
                          <label htmlFor="soundEffects">Enable sound notifications</label>
                        </div>
                        <p className="hint hide">
                          You can override your desktop notification preference on a case-by-case
                          basis for channels and groups from the channel or group menu.
                        </p>
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
