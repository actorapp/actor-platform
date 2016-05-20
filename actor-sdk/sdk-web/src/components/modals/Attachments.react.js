/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component } from 'react';
import { Container } from 'flux/utils';
import { findDOMNode } from 'react-dom';
import { FormattedMessage } from 'react-intl';
import Modal from 'react-modal';

import { KeyCodes } from '../../constants/ActorAppConstants';

import AttachmentsActionCreators from '../../actions/AttachmentsActionCreators';

import AttachmentsStore from '../../stores/AttachmentsStore';

import Attachment from './attachments/Attachment.react';
import Pagination from './attachments/Pagination.react';

class Attachments extends Component {
  static getStores() {
    return [AttachmentsStore];
  }

  static calculateState() {
    return {
      attachments: AttachmentsStore.getAllAttachments(),
      selectedIndex: AttachmentsStore.getSelectedIndex()
    }
  }

  constructor(props) {
    super(props);

    this.handleClose = this.handleClose.bind(this);
    this.handleKeyDown = this.handleKeyDown.bind(this);
    this.handleSelect = this.handleSelect.bind(this);
    this.handleCancel = this.handleCancel.bind(this);
    this.handleSend = this.handleSend.bind(this);
    this.handleSendAll = this.handleSendAll.bind(this);
  }

  componentWillMount() {
    document.addEventListener('keydown', this.handleKeyDown, false);
  }

  componentDidMount() {
    findDOMNode(this.refs.send).focus()
  }

  componentWillUnmount() {
    document.removeEventListener('keydown', this.handleKeyDown, false);
  }

  handleClose() {
    AttachmentsActionCreators.hide();
  }

  handleKeyDown(event) {
    if (event.keyCode === KeyCodes.ENTER) {
      event.preventDefault();
      if (event.shiftKey) {
        this.handleSendAll();
      } else {
        this.handleSend();
      }
    }
  }

  handleSelect(index) {
    AttachmentsActionCreators.selectAttachment(index);
  }

  handleCancel() {
    AttachmentsActionCreators.deleteAttachment();
  }

  handleSend() {
    AttachmentsActionCreators.sendAttachment();
  }

  handleSendAll() {
    AttachmentsActionCreators.sendAll(this.state.attachments);
  }

  renderHeaderButton() {
    const { attachments } = this.state;
    if (attachments.length <= 1) return null;

    return (
      <button className="button button--lightblue" onClick={this.handleSendAll}>
        <FormattedMessage id="button.sendAll"/>
      </button>
    );
  }

  renderAttachment() {
    const { attachments, selectedIndex } = this.state;
    if (attachments.length === 0) return null;

    return (
      <Attachment attachment={attachments[selectedIndex]}/>
    );
  }

  renderPagination() {
    const { attachments, selectedIndex } = this.state;
    if (attachments.length <= 1) return null;

    return (
      <div className="col-xs">
        <Pagination
          current={selectedIndex}
          total={attachments.length - 1}
          onChange={this.handleSelect}
        />
      </div>
    );
  }

  renderControls() {
    return (
      <div className="col-xs text-right">
        <button className="button" onClick={this.handleCancel}>
          <FormattedMessage id="button.cancel"/>
        </button>
        <button className="button button--rised" ref="send" onClick={this.handleSend}>
          <FormattedMessage id="button.send"/>
        </button>
      </div>
    );
  }

  render() {
    return (
      <Modal
        overlayClassName="modal-overlay"
        className="modal"
        onRequestClose={this.handleClose}
        isOpen>

        <div className="attachments">
          <div className="modal__content">

            <header className="modal__header">
              <FormattedMessage id="modal.attachments.title" tagName="h1"/>
              {this.renderHeaderButton()}
            </header>

            <div className="modal__body">
              {this.renderAttachment()}
            </div>

            <footer className="modal__footer row">
              {this.renderPagination()}
              {this.renderControls()}
            </footer>

          </div>
        </div>

      </Modal>
    );
  }
}

export default Container.create(Attachments);
