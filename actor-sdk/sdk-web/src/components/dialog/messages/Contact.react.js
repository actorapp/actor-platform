/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { map } from 'lodash';
import React, { Component, PropTypes } from 'react';
import classnames from 'classnames';

/**
 * Class that represents a component for display contact message content
 */
class Contact extends Component {
  constructor(props) {
    super(props);
  }

  static propTypes = {
    content: PropTypes.object.isRequired,
    className: PropTypes.string
  };

  render() {
    const { content, className } = this.props;
    const contactClassName = classnames(className, 'row');

    const contactAvatar = 'data:image/jpeg;base64,' + content.photo64;

    let emails, phones;
    if (content.emails.length > 0) {
      emails = map(content.emails, (email) => {
        return (
          <li className="contact__emails__item">
            <a href={'mailto:' + email}>{email}</a>
          </li>
        )
      })
    }
    // TODO: `pones` must be renamed to `phones` in library
    if (content.pones.length > 0) {
      phones = map(content.pones, (phone) => {
        return (
          <li className="contact__phones__item">
            <a href={'tel:' + phone}>{phone}</a>
          </li>
        )
      })
    }

    return (
      <div className={contactClassName}>
        <div className="contact row">
          <div className="contact__avatar">
            <img src={contactAvatar}
                 alt={content.name}/>
          </div>
          <div className="contact__body col-xs">
            <div className="contact__name">
              {content.name}
            </div>
            {
              content.emails.length > 0
                ? <ul className="contact__emails">{emails}</ul>
                : null
            }
            {
              content.pones.length > 0
                ? <ul className="contact__phones">{phones}</ul>
                : null
            }
          </div>
        </div>
        <div className="col-xs"/>
      </div>
    );
  }
}

export default Contact;
