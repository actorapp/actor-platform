/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React from 'react';
import classnames from 'classnames';
import ReactMixin from 'react-mixin';
import { IntlMixin } from 'react-intl';

export default
@ReactMixin.decorate(IntlMixin)
class Document extends React.Component {
  static propTypes = {
    content: React.PropTypes.object.isRequired,
    className: React.PropTypes.string
  };

  render() {
    const { content, className } = this.props;
    const documentClassName = classnames(className, 'row');

    return (
      <div className={documentClassName}>
        <div className="document row">
          <div className="document__icon">
            <i className="material-icons">attach_file</i>
          </div>
          <div className="col-xs">
            <span className="document__filename">{content.fileName}</span>
            <div className="document__meta">
              <span className="document__meta__size">{content.fileSize}</span>
              <span className="document__meta__ext">{content.fileExtension}</span>
            </div>
            <div className="document__actions">
              {
                content.isUploading
                  ? <span>{this.getIntlMessage('message.uploading')}</span>
                  : <a href={content.fileUrl}>{this.getIntlMessage('message.download')}</a>
              }
            </div>
          </div>
        </div>
        <div className="col-xs"></div>
      </div>
    );
  }
}
