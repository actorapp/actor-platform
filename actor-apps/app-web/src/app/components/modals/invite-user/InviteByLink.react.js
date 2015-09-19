/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { assign } from 'lodash';
import React from 'react';
import Modal from 'react-modal';
import addons from 'react/addons';
import ReactMixin from 'react-mixin';
import { IntlMixin, FormattedMessage } from 'react-intl';

import { KeyCodes } from 'constants/ActorAppConstants';

import InviteUserByLinkActions from 'actions/InviteUserByLinkActions';
import InviteUserActions from 'actions/InviteUserActions';

import InviteUserStore from 'stores/InviteUserStore';

const appElement = document.getElementById('actor-web-app');
Modal.setAppElement(appElement);

const {addons: { PureRenderMixin }} = addons;

const getStateFromStores = () => {
  return {
    isOpen: InviteUserStore.isInviteWithLinkModalOpen(),
    group: InviteUserStore.getGroup(),
    inviteUrl: InviteUserStore.getInviteUrl()
  };
};

@ReactMixin.decorate(IntlMixin)
@ReactMixin.decorate(PureRenderMixin)
class InviteByLink extends React.Component {
  constructor(props) {
    super(props);

    this.state = getStateFromStores();

    InviteUserStore.addChangeListener(this.onChange);
  }

  componentWillUnmount() {
    InviteUserStore.removeChangeListener(this.onChange);
  }

  componentWillUpdate(nextProps, nextState) {
    if (nextState.isOpen && !this.state.isOpen) {
      document.addEventListener('keydown', this.onKeyDown, false);
    } else if (this.state.isOpen && !nextState.isOpen) {
      document.removeEventListener('keydown', this.onKeyDown, false);
    }
  }

  render() {
    const { group, inviteUrl, isOpen } = this.state;

    const groupName = (group !== null) ? <b>{group.name}</b> : null;

    if (isOpen) {
      return (
        <Modal className="modal-new modal-new--invite-by-link"
               closeTimeoutMS={150}
               isOpen={isOpen}
               style={{width: 400}}>

          <header className="modal-new__header">
            <svg className="modal-new__header__icon icon icon--blue"
                 dangerouslySetInnerHTML={{__html: '<use xlink:href="assets/sprite/icons.svg#back"/>'}}
                 onClick={this.onBackClick}/>

            <h3 className="modal-new__header__title">
              <FormattedMessage message={this.getIntlMessage('inviteByLinkModalTitle')}/>
            </h3>

            <div className="pull-right">
              <button className="button button--lightblue" onClick={this.onClose}>Done</button>
            </div>
          </header>

          <div className="modal-new__body">
            <FormattedMessage groupName={groupName} message={this.getIntlMessage('inviteByLinkModalDescription')}/>
            <textarea className="invite-url" onClick={this.onInviteLinkClick} readOnly row="3" value={inviteUrl}/>
          </div>

          <footer className="modal-new__footer">
            <button className="button button--rised pull-left hide">
              <FormattedMessage message={this.getIntlMessage('inviteByLinkModalRevokeButton')}/>
            </button>
            <button className="button button--rised pull-right hide">
              <FormattedMessage message={this.getIntlMessage('inviteByLinkModalCopyButton')}/>
            </button>
          </footer>
        </Modal>
      );
    } else {
      return null;
    }
  }

  onChange = () => this.setState(getStateFromStores());
  onClose = () => InviteUserByLinkActions.hide();
  onInviteLinkClick = event => event.target.select();

  onBackClick = () => {
    const { group } = this.state;

    this.onClose();
    InviteUserActions.show(group);
  };

  onKeyDown = (event) => {
    if (event.keyCode === KeyCodes.ESC) {
      event.preventDefault();
      this.onClose();
    }
  };
}

export default InviteByLink;
