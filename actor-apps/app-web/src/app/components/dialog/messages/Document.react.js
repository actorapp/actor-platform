import React from 'react';
import classnames from 'classnames';

class Document extends React.Component {
  static propTypes = {
    content: React.PropTypes.object.isRequired,
    className: React.PropTypes.string
  };

  constructor(props) {
    super(props);
  }

  render() {
    const { content, className } = this.props;
    const documentClassName = classnames(className, 'row');

    let availableActions;
    if (content.isUploading === true) {
      availableActions = <span>Loading...</span>;
    } else {
      availableActions = <a href={content.fileUrl}>Download</a>;
    }

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
              {availableActions}
            </div>
          </div>
        </div>
        <div className="col-xs"></div>
      </div>
    );
  }
}

export default Document;
