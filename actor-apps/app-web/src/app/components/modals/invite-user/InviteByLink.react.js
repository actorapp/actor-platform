import React from 'react';
import Modal from 'react-modal';
import addons from 'react/addons';
import ReactMixin from 'react-mixin';
import { IntlMixin, FormattedMessage } from 'react-intl';
import { Styles, FlatButton, Snackbar } from 'material-ui';
import ReactZeroClipboard from 'react-zeroclipboard';

import { KeyCodes } from 'constants/ActorAppConstants';
import ActorTheme from 'constants/ActorTheme';

import InviteUserByLinkActions from 'actions/InviteUserByLinkActions';
import InviteUserActions from 'actions/InviteUserActions';

import InviteUserStore from 'stores/InviteUserStore';

const ThemeManager = new Styles.ThemeManager();

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

    InviteUserStore.addChangeListener(this.onChange);
    document.addEventListener('keydown', this.onKeyDown, false);
  }

  componentWillUnmount() {
    InviteUserStore.removeChangeListener(this.onChange);
    document.removeEventListener('keydown', this.onKeyDown, false);
  }

  render() {
    const group = this.state.group;
    const inviteUrl = this.state.inviteUrl;
    const isShown = this.state.isShown;
    const snackbarStyles = ActorTheme.getSnackbarStyles();

    let groupName;
    if (group !== null) {
      groupName = <b>{group.name}</b>;
    }

    return (
      <Modal className="modal-new modal-new--invite-by-link"
             closeTimeoutMS={150}
             isOpen={isShown}
             style={{width: 400}}>

        <header className="modal-new__header">
          <svg className="modal-new__header__icon icon icon--blue"
               dangerouslySetInnerHTML={{__html: '<use xlink:href="assets/sprite/icons.svg#back"/>'}}
               onClick={this.onBackClick}/>

          <h3 className="modal-new__header__title">
            <FormattedMessage message={this.getIntlMessage('inviteByLinkModalTitle')}/>
          </h3>
          <div className="pull-right">
            <FlatButton hoverColor="rgba(81,145,219,.17)"
                        label="Done"
                        labelStyle={{padding: '0 8px'}}
                        onClick={this.onClose}
                        secondary={true}
                        style={{marginTop: -6}}/>
          </div>
        </header>

        <div className="modal-new__body">
          <FormattedMessage groupName={groupName} message={this.getIntlMessage('inviteByLinkModalDescription')}/>
          <textarea className="invite-url" onClick={this.onInviteLinkClick} readOnly row="3" value={inviteUrl}/>
        </div>

        <footer className="modal-new__footer">
          <button className="button button--light-blue pull-left hide">
            <FormattedMessage message={this.getIntlMessage('inviteByLinkModalRevokeButton')}/>
          </button>
          <ReactZeroClipboard onCopy={this.onInviteLinkCopied} text={inviteUrl}>
            <button className="button button--blue pull-right">
              <FormattedMessage message={this.getIntlMessage('inviteByLinkModalCopyButton')}/>
            </button>
          </ReactZeroClipboard>
        </footer>

        <Snackbar autoHideDuration={3000}
                  message={this.getIntlMessage('integrationTokenCopied')}
                  ref="inviteLinkCopied"
                  style={snackbarStyles}/>
      </Modal>
    );
  }

  onClose = () => {
    InviteUserByLinkActions.hide();
  };

  onBackClick = () => {
    this.onClose();
    InviteUserActions.show(this.state.group);
  };

  onInviteLinkClick = event => {
    event.target.select();
  };

  onChange = () => {
    this.setState(getStateFromStores());
  };

  onKeyDown = (event) => {
    if (event.keyCode === KeyCodes.ESC) {
      event.preventDefault();
      this.onClose();
    }
  };

  onInviteLinkCopied = () => {
    this.refs.inviteLinkCopied.show();
  };

}

export default InviteByLink;
