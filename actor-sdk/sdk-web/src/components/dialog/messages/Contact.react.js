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
    name: PropTypes.string.isRequired,
    photo64: PropTypes.string.isRequired,
    emails: PropTypes.array.isRequired,
    pones: PropTypes.array.isRequired,
    className: PropTypes.string
  };

  render() {
    const { name, photo64, emails, pones, className } = this.props;
    const contactClassName = classnames(className, 'row');
    const isContactEmpty = emails.length === 0 && pones.length === 0;
    console.debug(isContactEmpty);

    const contactAvatar = photo64
      ? <div className="contact__avatar"><img src={'data:image/jpeg;base64,' + photo64} alt={name}/></div>
      : null;

    let emaislList = [], phonesList = [];
    if (emails.length > 0) {
      emaislList = map(emails, (email) => <li className="contact__emails__item"><a href={'mailto:' + email}>{email}</a></li>)
    }
    // TODO: `pones` must be renamed to `phones` in library
    if (pones.length > 0) {
      phonesList = map(pones, (phone) => <li className="contact__phones__item"><a href={'tel:' + phone}>{phone}</a></li>)
    }

    return (
      <div className={contactClassName}>
        {
          isContactEmpty
            ? <div className="contact contact--empty row">
                <i className="material-icons">error</i>Empty contact
              </div>
            : <div className="contact row">
                {contactAvatar}
                <div className="contact__body col-xs">
                  <div className="contact__name">{name}</div>
                  {
                    emaislList.length > 0
                      ? <ul className="contact__emails">{emaislList}</ul>
                      : null
                  }
                  {
                    phonesList.length > 0
                      ? <ul className="contact__phones">{phonesList}</ul>
                      : null
                  }
                </div>
              </div>
        }
        <div className="col-xs"/>
      </div>
    );
  }
}

export default Contact;
