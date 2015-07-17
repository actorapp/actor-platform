import React from 'react';
import Modal from 'react-modal';
//import pureRender from 'pure-render-decorator';
import { Styles, FlatButton } from 'material-ui';

import AppCacheStore from 'stores/AppCacheStore';
import AppCacheActionCreators from 'actions/AppCacheActionCreators';

import { KeyCodes } from 'constants/ActorAppConstants';
import ActorTheme from 'constants/ActorTheme';

const ThemeManager = new Styles.ThemeManager();

const appElement = document.getElementById('actor-web-app');
Modal.setAppElement(appElement);

const getStateFromStores = () => {
  return {
    isShown: AppCacheStore.isModalOpen()
  };
};

class AddContact extends React.Component {
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

    AppCacheStore.addChangeListener(this.onChange);
    document.addEventListener('keydown', this.onKeyDown, false);

    ThemeManager.setTheme(ActorTheme);
  }

  componentWillUnmount() {
    AppCacheStore.removeChangeListener(this.onChange);
    document.removeEventListener('keydown', this.onKeyDown, false);
  }

  render() {
    return (
      <Modal className="modal-new modal-new--update"
             closeTimeoutMS={150}
             isOpen={this.state.isShown}
             style={{width: 400}}>

        <div className="modal-new__body">
          <h1>Update available</h1>
          <h3>New version of Actor Web App available.</h3>
          <p>It's already downloaded to your browser, you just need to reload tab.</p>
        </div>

        <footer className="modal-new__footer text-right">
          <FlatButton hoverColor="rgba(74,144,226,.12)"
                      label="Cancel"
                      onClick={this.onClose}
                      secondary={true} />
          <FlatButton hoverColor="rgba(74,144,226,.12)"
                      label="Reload"
                      onClick={this.onConfirm}
                      secondary={true} />
        </footer>

      </Modal>
    );
  }

  onClose = () => {
    AppCacheActionCreators.closeModal();
  }

  onConfirm = () => {
    AppCacheActionCreators.confirmUpdate();
  }

  onChange = () => {
    this.setState(getStateFromStores());
  }

  onKeyDown = (event) => {
    if (event.keyCode === KeyCodes.ESC) {
      event.preventDefault();
      this.onClose();
    }
  }
}

export default AddContact;
