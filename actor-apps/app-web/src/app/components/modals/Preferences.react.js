import React from 'react';
import Modal from 'react-modal';
import ReactMixin from 'react-mixin';
import { IntlMixin, FormattedMessage } from 'react-intl';
import { Styles, FlatButton, RadioButtonGroup, RadioButton, DropDownMenu } from 'material-ui';

import { KeyCodes } from 'constants/ActorAppConstants';
import ActorTheme from 'constants/ActorTheme';

import PreferencesActionCreators from 'actions/PreferencesActionCreators';

import PreferencesStore from 'stores/PreferencesStore';

const appElement = document.getElementById('actor-web-app');
Modal.setAppElement(appElement);

const ThemeManager = new Styles.ThemeManager();

const getStateFromStores = () => {
  return {
    isOpen: PreferencesStore.isModalOpen()
  };
};

@ReactMixin.decorate(IntlMixin)
class PreferencesModal extends React.Component {
  static childContextTypes = {
    muiTheme: React.PropTypes.object
  };

  getChildContext() {
    return {
      muiTheme: ThemeManager.getCurrentTheme()
    };
  }

  constructor(props) {
    super(props);

    this.state = getStateFromStores();

    ThemeManager.setTheme(ActorTheme);
    ThemeManager.setComponentThemes({
      button: {
        minWidth: 60
      }
    });

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

  onKeyDown = event => {
    if (event.keyCode === KeyCodes.ESC) {
      event.preventDefault();
      this.onClose();
    }
  };

  render() {
    let menuItems = [
      { payload: '1', text: 'English' },
      { payload: '2', text: 'Russian' }
    ];

    if (this.state.isOpen === true) {
      return (
        <Modal className="modal-new modal-new--preferences"
               closeTimeoutMS={150}
               isOpen={this.state.isOpen}
               style={{width: 760}}>

          <div className="modal-new__header">
            <i className="modal-new__header__icon material-icons">settings</i>
            <h3 className="modal-new__header__title">
              <FormattedMessage message={this.getIntlMessage('preferencesModalTitle')}/>
            </h3>
            <div className="pull-right">
              <FlatButton hoverColor="rgba(81,145,219,.17)"
                          label="Done"
                          labelStyle={{padding: '0 8px'}}
                          onClick={this.onClose}
                          secondary={true}
                          style={{marginTop: -6}}/>
            </div>
          </div>

          <div className="modal-new__body">
            <div className="preferences">
              <aside className="preferences__tabs">
                <a className="preferences__tabs__tab preferences__tabs__tab--active">General</a>
                <a className="preferences__tabs__tab">Notifications</a>
                <a className="preferences__tabs__tab">Sidebar colors</a>
                <a className="preferences__tabs__tab">Security</a>
                <a className="preferences__tabs__tab">Other Options</a>
              </aside>
              <div className="preferences__body">
                <div className="preferences__list">
                  <div className="preferences__list__item  preferences__list__item--general">
                    <ul>
                      <li>
                        <i className="icon material-icons">keyboard</i>
                        <RadioButtonGroup defaultSelected="default" name="send">
                          <RadioButton label="Enter – send message, Shift + Enter – new line"
                                       style={{marginBottom: 12}}
                                       value="default"/>
                          <RadioButton label="Cmd + Enter – send message, Enter – new line"
                                       //style={{marginBottom: 16}}
                                       value="alt"/>
                        </RadioButtonGroup>
                      </li>
                      <li className="language">
                        <i className="icon material-icons">menu</i>
                        Language: <DropDownMenu labelStyle={{color: '#5191db'}}
                                                menuItemStyle={{height: '40px', lineHeight: '40px'}}
                                                menuItems={menuItems}
                                                style={{verticalAlign: 'top', height: 52}}
                                                underlineStyle={{display: 'none'}}/>
                      </li>
                    </ul>
                  </div>
                  <div className="preferences__list__item preferences__list__item--notifications">
                    <ul>
                      <li>
                        <i className="icon material-icons">notifications</i>
                        <RadioButtonGroup defaultSelected="all" name="notifications">
                          <RadioButton label="Notifications for activity of any kind"
                                       style={{marginBottom: 12}}
                                       value="all"/>
                          <RadioButton label="Notifications for Highlight Words and direct messages"
                                       style={{marginBottom: 12}}
                                       value="quiet"/>
                          <RadioButton label="Never send me notifications"
                                       style={{marginBottom: 12}}
                                       value="disable"/>
                        </RadioButtonGroup>
                        <p className="hint">
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
