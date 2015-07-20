import React from 'react';

import SettingsActionCreators from 'actions/SettingsActionCreators';
import SettingsStore from 'stores/SettingsStore';

import { Styles, Tabs, Tab, RaisedButton } from 'material-ui';
import ActorTheme from 'constants/ActorTheme';
const ThemeManager = new Styles.ThemeManager();

import { KeyCodes } from 'constants/ActorAppConstants';

import Modal from 'react-modal';

const appElement = document.getElementById('actor-web-app');
Modal.setAppElement(appElement);

const getStateFromStores = () => {
  return {
    isOpen: SettingsStore.isModalOpen()
  };
};

class SettingsModal extends React.Component {
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

    SettingsStore.addChangeListener(this.onChange);
    document.addEventListener('keydown', this.onKeyDown, false);
  }

  componentWillUnmount() {
    SettingsStore.removeChangeListener(this.onChange);
    document.removeEventListener('keydown', this.onKeyDown, false);
  }

  onChange = () => {
    this.setState(getStateFromStores());
  }

  onClose = () => {
    SettingsActionCreators.hide();
  }

  onKeyDown = event => {
    if (event.keyCode === KeyCodes.ESC) {
      event.preventDefault();
      this.onClose();
    }
  }

  render() {
    if (this.state.isOpen === true) {
      return (
        <Modal className="modal modal--settings"
               closeTimeoutMS={150}
               isOpen={this.state.isOpen}
               style={{width: 600}}>


            <header className="modal__header">
              <h3>Settings</h3>
              <RaisedButton label="Save" style={{float: 'right'}} onClick={this.onClose}/>
            </header>
            <Tabs className="modal__tabs"
                  contentContainerClassName="modal__tabs__tab-content"
                  tabItemContainerClassName="modal__tabs__tab-items">
              <Tab label="Sidebar">
                <h1>Sidebar</h1>
              </Tab>
              <Tab label="State">
                <h1>State</h1>
              </Tab>
              <Tab label="Sounds">
                <h1>Sounds</h1>
              </Tab>
              <Tab label="Media">
                <h1>Media</h1>
              </Tab>
              <Tab label="Files">
                <h1>Files</h1>
              </Tab>
              <Tab label="Emoji">
                <h1>Emoji</h1>
              </Tab>
            </Tabs>

        </Modal>
      );
    } else {
      return null;
    }
  }
}

export default SettingsModal;
