/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component } from 'react';
import Modal from 'react-modal';
import { FormattedMessage, FormattedHTMLMessage } from 'react-intl';
import { Container } from 'flux/utils';

import InviteUserByLinkActions from '../../actions/InviteUserByLinkActions';

import InviteUserStore from '../../stores/InviteUserStore';

import SvgIcon from '../common/SvgIcon.react';

class InviteByLink extends Component {
  static getStores() {
    return [InviteUserStore];
  }

  static calculateState() {
    return {
      group: InviteUserStore.getGroup(),
      inviteUrl: InviteUserStore.getInviteUrl()
    };
  }

  constructor(props) {
    super(props);

    this.handleClose = this.handleClose.bind(this);
    this.handleInviteLinkSelect = this.handleInviteLinkSelect.bind(this);
    this.handleBackClick = this.handleBackClick.bind(this);
  }

  handleClose() {
    InviteUserByLinkActions.hide();
  }

  handleInviteLinkSelect(event) {
    event.target.select();
  }

  handleBackClick() {
    this.handleClose();
  }

  renderControls() {
    {/* TODO : Implement token copy and revoke functional */}
    return (
      <footer className="modal__footer">
        <button className="button button--rised pull-left hide">
          <FormattedMessage id="invite.byLink.revoke"/>
        </button>
        <button className="button button--rised pull-right hide">
          <FormattedMessage id="invite.byLink.copy"/>
        </button>
      </footer>
    );
  }

  render() {
    const { group, inviteUrl } = this.state;

    return (
      <Modal
        overlayClassName="modal-overlay"
        className="modal"
        onRequestClose={this.handleClose}
        isOpen>

        <div className="invite-by-link">
          <div className="modal__content">

            <header className="modal__header">
              <SvgIcon
                className="modal__header__icon icon icon--blue"
                glyph="back"
                onClick={this.handleBackClick}/>
              <FormattedMessage id="invite.byLink.title" tagName="h1"/>
            </header>

            <div className="modal__body">
              <FormattedHTMLMessage id="invite.byLink.description" values={{ groupName: group.name }}/>
              <textarea
                className="textarea"
                onClick={this.handleInviteLinkSelect}
                readOnly
                row="3"
                value={inviteUrl}/>
            </div>

            {/*{this.renderControls()}*/}

          </div>
        </div>

      </Modal>
    );
  }
}

export default Container.create(InviteByLink);
