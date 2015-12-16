/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import classnames from 'classnames';

/**
 * Class that represents component for display modern text messages content
 * @param {object} paragraphStyle Contains message styles
 * @param {string} text Message text
 * @param {string} className Component class name
 */
class TextModern extends Component {
  static propTypes = {
    attaches: PropTypes.object.isRequired,
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
      borderColor: paragraphStyle.color ? paragraphStyle.color.name : 'transparent',
      backgroundColor: paragraphStyle.bgColor ? paragraphStyle.bgColor.name : 'transparent',
      color: paragraphStyle.color ? paragraphStyle.color.name : 'inherit'
    };

    return (
      <div className={className}>
        <div className={modernClassName} style={modernStyles}>
          {
            paragraphStyle.showParagraph
              ? <p>{text}</p>
              : {text}
          }
        </div>
      </div>
    );
  }
}

export default TextModern;
