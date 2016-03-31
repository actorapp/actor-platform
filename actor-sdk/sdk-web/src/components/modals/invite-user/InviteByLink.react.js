/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import Modal from 'react-modal';
import { FormattedMessage } from 'react-intl';
import { Container } from 'flux/utils';

import { escapeWithEmoji } from '../../../utils/EmojiUtils'

import { KeyCodes } from '../../../constants/ActorAppConstants';

import SvgIcon from '../../common/SvgIcon.react';

import InviteUserByLinkActions from '../../../actions/InviteUserByLinkActions';
import InviteUserActions from '../../../actions/InviteUserActions';

import InviteUserStore from '../../../stores/InviteUserStore';

class InviteByLink extends Component {
  static contextTypes = {
    intl: PropTypes.object
  };

  static getStores() {
    return [InviteUserStore];
  }

  static calculateState() {
    return {
      isOpen: InviteUserStore.isInviteWithLinkModalOpen(),
      group: InviteUserStore.getGroup(),
      inviteUrl: InviteUserStore.getInviteUrl()
    };
  }

  componentWillUpdate(nextProps, nextState) {
    if (nextState.isOpen && !this.state.isOpen) {
      document.addEventListener('keydown', this.onKeyDown, false);
    } else if (this.state.isOpen && !nextState.isOpen) {
      document.removeEventListener('keydown', this.onKeyDown, false);
    }
  }

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

  render() {
    const { group, inviteUrl, isOpen } = this.state;
    const { intl } = this.context;

    const groupName = (group !== null) ? <b dangerouslySetInnerHTML={{__html: escapeWithEmoji(group.name)}}/> : null;

    const modalStyle = {
      content : {
        position: null,
        top: null,
        left: null,
        right: null,
        bottom: null,
        border: null,
        background: null,
        overflow: null,
        outline: null,
        padding: null,
        borderRadius: null,
        width: 440
      }
    };

    if (isOpen) {
      return (
        <Modal className="modal-new modal-new--invite-by-link"
               closeTimeoutMS={150}
               isOpen={isOpen}
               style={modalStyle}>

          <header className="modal-new__header">

            <SvgIcon
              className="modal-new__header__icon icon icon--blue"
              glyph="back"
              onClick={this.onBackClick}
            />

            <h3 className="modal-new__header__title">
              {intl.messages['inviteByLinkModalTitle']}
            </h3>

            <div className="pull-right">
              <button className="button button--lightblue" onClick={this.onClose}>{intl.messages['button.done']}</button>
            </div>
          </header>

          <div className="modal-new__body">
            <FormattedMessage id="inviteByLinkModalDescription" values={{groupName}}/>
            <textarea className="textarea" onClick={this.onInviteLinkClick} readOnly row="3" value={inviteUrl}/>
          </div>

          <footer className="modal-new__footer">
            <button className="button button--rised pull-left hide">
              {intl.messages['inviteByLinkModalRevokeButton']}
            </button>
            <button className="button button--rised pull-right hide">
              {intl.messages['inviteByLinkModalCopyButton']}
            </button>
          </footer>
        </Modal>
      );
    } else {
      return null;
    }
  }
}

export default Container.create(InviteByLink);
