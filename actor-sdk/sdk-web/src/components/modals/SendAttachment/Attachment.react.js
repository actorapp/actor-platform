/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';

import humanFileSize from '../../../utils/humanFileSize';

import AttachmentsActionCreators from '../../../actions/AttachmentsActionCreators';

class Attachment extends Component {
  constructor(props) {
    super(props);
  }

  static propTypes = {
    attachment: PropTypes.object.isRequired
  };

  static contextTypes = {
    intl: PropTypes.object
  };

  changeAttachment = () => {
    const { sendAsPicture } = this.props.attachment;
    AttachmentsActionCreators.changeAttachment(!sendAsPicture);
  };

  render() {
    const { attachment } = this.props;
    const { intl } = this.context;

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
            <div className="attachment__meta__title">{intl.messages['modal.attachments.name']}</div>
            <div className="attachment__meta__content">{attachment.file.name}</div>
          </div>
          <div className="row">
            <div className="col-xs">
              <div className="attachment__meta attachment__meta--size">
                <div className="attachment__meta__title">{intl.messages['modal.attachments.type']}</div>
                <div className="attachment__meta__content">{attachment.file.type}</div>
              </div>
            </div>
            <div className="col-xs">
              <div className="attachment__meta attachment__meta--size">
                <div className="attachment__meta__title">{intl.messages['modal.attachments.size']}</div>
                <div className="attachment__meta__content">{humanFileSize(attachment.file.size, true)}</div>
              </div>
            </div>
          </div>

          {
            attachment.isImage
              ? <div className="attachment__extra">
                  <div className="attachment__extra__title">{intl.messages['modal.attachments.extra']}</div>
                  <div className="attachment__extra__switcher">
                    <label htmlFor="sendAsPicture" className="switch-label">{intl.messages['modal.attachments.sendAsPicture']}</label>
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

export default Attachment;
