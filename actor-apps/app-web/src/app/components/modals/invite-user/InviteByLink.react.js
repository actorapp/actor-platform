import React from 'react';
import Modal from 'react-modal';
import addons from 'react/addons';
import ReactMixin from 'react-mixin';
import { IntlMixin, FormattedMessage } from 'react-intl';
//import { Styles } from 'material-ui';

import { KeyCodes } from 'constants/ActorAppConstants';
//import ActorTheme from 'constants/ActorTheme';

import InviteUserByLinkActions from 'actions/InviteUserByLinkActions';
import InviteUserActions from 'actions/InviteUserActions';

import InviteUserStore from 'stores/InviteUserStore';

//const ThemeManager = new Styles.ThemeManager();

const appElement = document.getElementById('actor-web-app');
Modal.setAppElement(appElement);

const {addons: { PureRenderMixin }} = addons;

const getStateFromStores = () => {
  return {
    isShown: InviteUserStore.isInviteWithLinkModalOpen(),
    group: InviteUserStore.getGroup(),
    inviteUrl: InviteUserStore.getInviteUrl()
  };
};

@ReactMixin.decorate(IntlMixin)
@ReactMixin.decorate(PureRenderMixin)
class InviteByLink extends React.Component {
  //static childContextTypes = {
  //  muiTheme: React.PropTypes.object
  //};
  //
  //getChildContext() {
  //  return {
  //    muiTheme: ThemeManager.getCurrentTheme()
  //  };
  //}

  constructor(props) {
    console.warn('constructor');
    super(props);

    this.state = getStateFromStores();

    //ThemeManager.setTheme(ActorTheme);
    InviteUserStore.addChangeListener(this.onChange);
    document.addEventListener('keydown', this.onKeyDown, false);
  }

  componentWillUnmount() {
    InviteUserStore.removeChangeListener(this.onChange);
    document.removeEventListener('keydown', this.onKeyDown, false);
  }

  render() {
    return (
      <Modal className="modal-new modal-new--invite-link"
             closeTimeoutMS={150}
             isOpen={this.state.isShown}
             style={{width: 320}}>

        <header className="modal-new__header">
          <a className="modal-new__header__close material-icons"
             onClick={this.onClose}>keyboard_backspace</a>

          <h3 className="modal-new__header__title">
            <FormattedMessage message={this.getIntlMessage('inviteByLinkModalTitle')}/>
          </h3>
        </header>

        <div className="modal-new__body">
          <FormattedMessage message={this.getIntlMessage('inviteByLinkModalDescription')}/>
          {this.state.inviteUrl}
        </div>

        <footer className="modal-new__footer text-right">
          <button className="button">
            <FormattedMessage message={this.getIntlMessage('inviteByLinkModalRevokeButton')}/>
          </button>
          <button className="button">
            <FormattedMessage message={this.getIntlMessage('inviteByLinkModalCopyButton')}/>
          </button>
        </footer>
      </Modal>
    );
  }

  onClose = () => {
    InviteUserByLinkActions.hide();
    InviteUserActions.show(this.state.group);
  };

  onChange = () => {
    this.setState(getStateFromStores());
  };

  onKeyDown = (event) => {
    if (event.keyCode === KeyCodes.ESC) {
      event.preventDefault();
      this.onClose();
    }
  }
}

export default InviteByLink;
