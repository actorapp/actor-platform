/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { map } from 'lodash';
import React, { Component, PropTypes } from 'react';
import classnames from 'classnames';

const ColorTypes = {
  HEX: 'hex',
  NAMED: 'named'
};

/**
 * @param color
 * @returns {string | null} Color value
 */
function getColor(color) {
  if (color) {
    switch (color.type) {
      case ColorTypes.HEX:
        return color.hex;
      case ColorTypes.NAMED:
        return color.name;
      default:
        return null;
    }
  } else {
    return null;
  }
}

/**
 * Class that represents component for display modern text message attachment field
 * @param {string} title Field title
 * @param {string} value Field value
 * @param {bool} isShort Display short version of field
 */
class Field extends Component {
  static propTypes = {
    title: PropTypes.string.isRequired,
    value: PropTypes.string.isRequired,
    isShort: PropTypes.bool.isRequired
  };

  constructor(props) {
    super(props);
  }

  render() {
    const { title, value, isShort } = this.props;

    const fieldClassName = classnames('field', {
      'field--short': isShort,
      'col-xs-6': isShort,
      'col-xs-12': !isShort
    });

    return (
      <div className={fieldClassName}>
        {title ? <div className="field__title">{title}</div> : null}
        {value ? <div className="field__value">{value}</div> : null}
      </div>
    )
  }
}

/**
 * Class that represents component for display modern text message attachments
 * @param {array} fields Array of objects contains attachment fields
 * @param {object} paragraphStyle Contains attachment styles
 * @param {string} text Attachment text
 * @param {string} title Attachment title
 * @param {string} titleUrl Attachment title url
 */
class Attach extends Component {
  static propTypes = {
    paragraphStyle: PropTypes.object.isRequired,
    text: PropTypes.string.isRequired,
    title: PropTypes.string.isRequired,
    titleUrl: PropTypes.string.isRequired,
    fields: PropTypes.array.isRequired
  };

  constructor(props) {
    super(props);
  }

  render() {
    const { paragraphStyle, titleUrl, title, text, fields } = this.props;

    const attachmentClassName = classnames('attachment', {
      'attachment--paragraph': paragraphStyle.showParagraph
    });

    const attachmentStyles = {
      borderColor: getColor(paragraphStyle.color) || 'transparent',
      backgroundColor: getColor(paragraphStyle.bgColor) || 'transparent'
    };

    const visibleTitle = titleUrl ? <a href={titleUrl}>{title}</a> : {title};

    const attachmentFields = map(fields, (field, index) => <Field key={index} {...field}/>);

    return (
      <div className={attachmentClassName} style={attachmentStyles}>
        <div className="attachment__title">
          {visibleTitle}
        </div>
        {text ? text : null}
        {attachmentFields ? <div className="attachment_fields row">{attachmentFields}</div> : null}
      </div>
    );
  }
}

/**
 * Class that represents component for display modern text messages content
 * @param {array} attaches Array of objects contains modern message attached paragraphs
 * @param {object} paragraphStyle Contains message styles
 * @param {string} text Message text
 * @param {string} className Component class name
 */
class TextModern extends Component {
  static propTypes = {
    attaches: PropTypes.array.isRequired,
    paragraphStyle: PropTypes.object.isRequired,
    text: PropTypes.string.isRequired,
    className: PropTypes.string
  };

  constructor(props) {
    super(props);
  }

  render() {
    const { paragraphStyle, attaches, text, className } = this.props;
    const modernClassName = classnames('modern', {
      'modern--paragraph': paragraphStyle.showParagraph
    });

    const modernStyles = {
      borderColor: getColor(paragraphStyle.color) || 'transparent',
      backgroundColor: getColor(paragraphStyle.bgColor) || 'transparent'
    };

    const modernAttachments = map(attaches, (attachment, index) => <Attach key={index} {...attachment}/>);

    return (
      <div className={className}>
        <div className={modernClassName} style={modernStyles}>
          {text ? <p>{text}</p> : null}
          {modernAttachments}
        </div>
      </div>
    );
  }
}

export default TextModern;
