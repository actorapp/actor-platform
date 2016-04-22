/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { shouldComponentUpdate } from 'react-addons-pure-render-mixin';
import { FormattedMessage } from 'react-intl';
import { escapeWithEmoji } from '../../../utils/EmojiUtils';

import AvatarItem from '../../common/AvatarItem.react';

class ContactItem extends Component {
  static propTypes = {
    id: PropTypes.number.isRequired,
    name: PropTypes.string.isRequired,
    nick: PropTypes.string,
    avatar: PropTypes.string,
    about: PropTypes.string,
    placeholder: PropTypes.string.isRequired,
    emails: PropTypes.array,
    phones: PropTypes.array,

    isBot: PropTypes.bool.isRequired,
    isContact: PropTypes.bool.isRequired,
    isOnline: PropTypes.bool.isRequired,

    onSelect: React.PropTypes.func
  };

  constructor(props) {
    super(props);

    this.shouldComponentUpdate = shouldComponentUpdate.bind(this);
    this.handleClick = this.handleClick.bind(this);
  }

  handleClick(event) {
    event.preventDefault();
    const { onSelect, id, isContact } = this.props;
    onSelect && onSelect(id, isContact)
  }

  renderAbout() {
    const { about } = this.props;
    if (!about) return null;

    return (
      <div className="about row">
        <div className="title"><FormattedMessage id="profile.about"/>:</div>
        <div className="col-xs">{about}</div>
      </div>
    );
  }

  renderEmails() {
    const { emails } = this.props;
    if (emails.length === 0) return null;

    return emails.map((email, index) => {
      return (
        <div className="email row" key={`e${index}`}>
          <div className="title"><FormattedMessage id="profile.email"/>:</div>
          <div className="col-xs">
            <a href={'mailto:' + email.email}>{email.email}</a>
          </div>
        </div>
      );
    });
  }

  renderPhones() {
    const { phones } = this.props;
    if (phones.length === 0) return null;

    return phones.map((phone, index) => {
      return (
        <div className="phone row" key={`p${index}`}>
          <div className="title"><FormattedMessage id="profile.phone"/>:</div>
          <div className="col-xs">
            <a href={'tel:' + phone.number}>{phone.number}</a>
          </div>
        </div>
      );
    });
  }

  renderControls() {
    const { isContact } = this.props;
    return (
      <button className="button button--rised" onClick={this.handleClick}>
        <FormattedMessage id={isContact ? 'modal.quickSearch.openDialog' : 'addToContacts'}/>
      </button>
    );
  }


  render() {
    const { name, nick, avatar, placeholder, isBot } = this.props;

    return (
      <li className="add-contact__results__item contact">
        <div className="row">
          <AvatarItem image={avatar}
                      placeholder={placeholder}
                      size="large"
                      title={name}/>

          <div className="col-xs">
            <div className="name">
              <span dangerouslySetInnerHTML={{ __html: escapeWithEmoji(name) }}/>
              {isBot ? <small>BOT</small> : null}
            </div>
            <div className="nick">{nick}</div>
          </div>
        </div>

        <div className="meta">
          {this.renderAbout()}
          {this.renderEmails()}
          {this.renderPhones()}
        </div>

        <div className="controls">
          {this.renderControls()}
        </div>
      </li>
    );
  }
}

export default ContactItem;
