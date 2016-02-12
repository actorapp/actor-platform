/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import classnames from 'classnames';

/**
 * Class that represents a component for display document message content
 */
class Document extends Component {
  constructor(props) {
    super(props);
  }

  static propTypes = {
    content: PropTypes.object.isRequired,
    className: PropTypes.string
  };

  static contextTypes = {
    intl: PropTypes.object
  };

  render() {
    const { content, className } = this.props;
    const { intl } = this.context;

    const documentClassName = classnames(className, 'row');

    return (
      <div className={documentClassName}>
        <div className="document row">
          {
            content.isUploading
              ? <div className="document__icon">
                  <i className="material-icons">attach_file</i>
                </div>
              : <a className="document__icon" href={content.fileUrl}>
                  <i className="material-icons">attach_file</i>
                </a>
          }
          <div className="col-xs">
            <span className="document__filename">{content.fileName}</span>
            <div className="document__meta">
              <span className="document__meta__size">{content.fileSize}</span>
              <span className="document__meta__ext">{content.fileExtension}</span>
            </div>
            <div className="document__actions">
              {
                content.isUploading
                  ? <span>{intl.messages['message.uploading']}</span>
                  : <a href={content.fileUrl}>{intl.messages['message.download']}</a>
              }
            </div>
          </div>
        </div>
        <div className="col-xs"></div>
      </div>
    );
  }
}

export default Document;
