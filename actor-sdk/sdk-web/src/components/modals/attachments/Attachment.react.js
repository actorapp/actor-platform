/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { FormattedMessage } from 'react-intl';

import humanFileSize from '../../../utils/humanFileSize';

import AttachmentsActionCreators from '../../../actions/AttachmentsActionCreators';

class Attachment extends Component {
  static propTypes = {
    attachment: PropTypes.object.isRequired
  };

  constructor(props) {
    super(props);

    this.handleChangeSendAsPicture = this.handleChangeSendAsPicture.bind(this);
   }

  handleChangeSendAsPicture() {
    const { sendAsPicture } = this.props.attachment;
    AttachmentsActionCreators.changeAttachment(!sendAsPicture);
  }

  renderPreview() {
    const { attachment } = this.props;
    if (!attachment.isImage) return null;

    return (
      <div className="wrapper">
        <img src={window.URL.createObjectURL(attachment.file)}/>
      </div>
    );
  }

  renderName() {
    const { attachment } = this.props;

    return (
      <div className="attachment__meta attachment__meta--name">
        <div className="attachment__meta__title"><FormattedMessage id="modal.attachments.name"/></div>
        <div className="attachment__meta__content">{attachment.file.name}</div>
      </div>
    );
  }

  renderType() {
    const { attachment } = this.props;

    return (
      <div className="attachment__meta attachment__meta--type">
        <div className="attachment__meta__title"><FormattedMessage id="modal.attachments.type"/></div>
        <div className="attachment__meta__content">{attachment.file.type}</div>
      </div>
    );
  }

  renderSize() {
    const { attachment } = this.props;

    return (
      <div className="attachment__meta attachment__meta--size">
        <div className="attachment__meta__title"><FormattedMessage id="modal.attachments.size"/></div>
        <div className="attachment__meta__content">{humanFileSize(attachment.file.size, true)}</div>
      </div>
    );
  }

  renderOptions() {
    const { attachment } = this.props;
    if (!attachment.isImage) return null;

    return (
      <div className="attachment__extra">
        <div className="attachment__extra__title">
        <FormattedMessage id="modal.attachments.extra"/></div>
        <div className="attachment__extra__switcher">
          <label htmlFor="sendAsPicture" className="switch-label"><FormattedMessage id="modal.attachments.sendAsPicture"/></label>
          <div className="switch pull-right">
            <input
              checked={attachment.sendAsPicture}
              id="sendAsPicture"
              onChange={this.handleChangeSendAsPicture}
              type="checkbox"/>
            <label htmlFor="sendAsPicture"/>
          </div>
        </div>
      </div>
    );
  }

  render() {
    return (
      <div className="attachment row">

        <div className="attachment__preview col-xs-5">
          {this.renderPreview()}
        </div>

        <div className="col-xs-7" style={{ paddingLeft: 16 }}>

          {this.renderName()}

          <div className="row">
            <div className="col-xs">
              {this.renderType()}
            </div>
            <div className="col-xs">
              {this.renderSize()}
            </div>
          </div>

          {this.renderOptions()}

        </div>
      </div>
    )
  }
}

export default Attachment;
