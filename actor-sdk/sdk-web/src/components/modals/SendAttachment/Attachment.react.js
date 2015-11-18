/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import Modal from 'react-modal';
import ReactMixin from 'react-mixin';
import { IntlMixin } from 'react-intl';

import { KeyCodes } from '../../../constants/ActorAppConstants';
import humanFileSize from '../../../utils/humanFileSize';

import AttachmentsActionCreators from '../../../actions/AttachmentsActionCreators';

import AttachmentStore from '../../../stores/AttachmentStore';

class Attachment extends Component {
  constructor(props) {
    super(props);
  }

  static propTypes = {
    attachment: PropTypes.object.isRequired
  };

  changeAttachment = () => {
    const { sendAsPicture } = this.props.attachment;
    AttachmentsActionCreators.changeAttachment(!sendAsPicture);
  };

  render() {
    const { attachment } = this.props;

    return (
      <div className="attachment row">

        <div className="attachment__preview col-xs-5">
          {
            attachment.isImage
              ? <img src={window.URL.createObjectURL(attachment.file)}/>
              : null
          }
        </div>

        <div className="col-xs-7" style={{paddingLeft: 16}}>
          <div className="attachment__meta attachment__meta--name">
            <div className="attachment__meta__title">{this.getIntlMessage('modal.attachments.name')}</div>
            <div className="attachment__meta__content">{attachment.file.name}</div>
          </div>
          <div className="row">
            <div className="col-xs">
              <div className="attachment__meta attachment__meta--size">
                <div className="attachment__meta__title">{this.getIntlMessage('modal.attachments.type')}</div>
                <div className="attachment__meta__content">{attachment.file.type}</div>
              </div>
            </div>
            <div className="col-xs">
              <div className="attachment__meta attachment__meta--size">
                <div className="attachment__meta__title">{this.getIntlMessage('modal.attachments.size')}</div>
                <div className="attachment__meta__content">{humanFileSize(attachment.file.size, true)}</div>
              </div>
            </div>
          </div>

          {
            attachment.isImage
              ? <div className="attachment__extra">
                  <div className="attachment__extra__title">{this.getIntlMessage('modal.attachments.extra')}</div>
                  <div className="attachment__extra__switcher">
                    <label htmlFor="sendAsPicture" className="switch-label">{this.getIntlMessage('modal.attachments.sendAsPicture')}</label>
                    <div className="switch pull-right">
                      <input checked={attachment.sendAsPicture}
                             id="sendAsPicture"
                             onChange={this.changeAttachment}
                             type="checkbox"/>
                      <label htmlFor="sendAsPicture"/>
                    </div>
                  </div>
                </div>
              : null
          }

        </div>
      </div>
    )
  }
}

ReactMixin.onClass(Attachment, IntlMixin);

export default Attachment;
