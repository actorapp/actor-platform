/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import { forEach } from 'lodash';

import React, { Component, PropTypes } from 'react';
import { Container } from 'flux/utils';
import { findDOMNode } from 'react-dom';
import Modal from 'react-modal';

import { KeyCodes } from '../../../constants/ActorAppConstants';

import AttachmentsActionCreators from '../../../actions/AttachmentsActionCreators';

import AttachmentStore from '../../../stores/AttachmentStore';

import Attachment from './Attachment.react';
import Pagination from './Pagination.react';

class SendAttachment extends Component {
  constructor(props) {
    super(props);
  }

  static getStores = () => [AttachmentStore];

  static calculateState() {
    return {
      isOpen: AttachmentStore.isOpen(),
      attachments: AttachmentStore.getAllAttachments(),
      selectedIndex: AttachmentStore.getSelectedIndex()
    }
  }

  static contextTypes = {
    intl: PropTypes.object
  };

  componentWillMount() {
    document.addEventListener('keydown', this.handleKeyDown, false);
  }
  componentDidMount() {
    findDOMNode(this.refs.send).focus()
  }

  componentWillUnmount() {
    document.removeEventListener('keydown', this.handleKeyDown, false);
  }

  handleClose = () => AttachmentsActionCreators.hide();

  handleKeyDown = (event) => {
    event.preventDefault();
    switch (event.keyCode) {
      case KeyCodes.ESC:
        this.handleClose();
        break;
      case KeyCodes.ENTER:
        if (event.shiftKey) {
          this.handleSendAll();
        } else {
          this.handleSend();
        }
        break;
    }
  };

  handleSelect = (index) => AttachmentsActionCreators.selectAttachment(index);

  handleCancel = () => AttachmentsActionCreators.deleteAttachment();

  handleSend = () => AttachmentsActionCreators.sendAttachment(AttachmentStore.getAttachment(), this.state.selectedIndex);

  handleSendAll = () => AttachmentsActionCreators.sendAll(this.state.attachments);

  render() {
    const { isOpen, attachments, selectedIndex } = this.state;
    const { intl } = this.context;
    const isSingleFile = attachments.length > 1;

    return (
      <Modal className="modal-new modal-new--attachments"
             closeTimeoutMS={150}
             isOpen={isOpen}
             style={{width: 700}}>

        <header className="modal-new__header">
          <h3 className="modal-new__header__title">{intl.messages['modal.attachments.title']}</h3>
          {
            isSingleFile
              ? <button className="button button--lightblue pull-right"
                        onClick={this.handleSendAll}>{intl.messages['button.sendAll']}</button>
              : null
          }
        </header>

        <section className="modal-new__body">
          <Attachment attachment={attachments[selectedIndex]}/>
        </section>

        <footer className="modal-new__footer row">
          <div className="col-xs">
            {
              isSingleFile
                ? <Pagination current={selectedIndex}
                              total={attachments.length - 1}
                              onChange={this.handleSelect}/>
                : null
            }
          </div>

          <div className="col-xs text-right">
            <button className="button"
                    onClick={this.handleCancel}>{intl.messages['button.cancel']}</button>
            <button className="button button--rised" ref="send"
                    onClick={this.handleSend}>{intl.messages['button.send']}</button>
          </div>
        </footer>
      </Modal>
    );
  }
}

export default Container.create(SendAttachment, {pure: false});
