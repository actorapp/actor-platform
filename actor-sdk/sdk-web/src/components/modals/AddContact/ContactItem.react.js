/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { shouldComponentUpdate } from 'react-addons-pure-render-mixin';
import AvatarItem from '../../common/AvatarItem.react';
import { FormattedMessage } from 'react-intl';
import { escapeWithEmoji } from '../../../utils/EmojiUtils';

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
  }

  render() {
    const { name, avatar, about, placeholder, nick, emails, phones, isBot, isContact } = this.props;

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
              {
                isBot
                  ? <small>BOT</small>
                  : null
              }
            </div>
            <div className="nick">{nick}</div>
          </div>
        </div>

        <div className="meta">
          {
            about
              ? <div className="about">
                  <div className="title"><FormattedMessage id="profile.about"/>:</div>
                  {about}
                </div>
              : null
          }
          {
            emails[0]
              ? <div className="email">
                  <div className="title"><FormattedMessage id="profile.email"/>:</div>
                  <a href={'mailto:' + emails[0].email}>{emails[0].email}</a>
                </div>
              : null
          }
          {
            phones[0]
              ? <div className="phone">
                  <div className="title"><FormattedMessage id="profile.phone"/>:</div>
                  <a href={'tel:' + phones[0].email}>{phones[0].number}</a>
                </div>
              : null
          }
        </div>
        <div className="controls">
          <button className="button button--rised" onClick={this.handleClick}>
          {
            isContact
              ? <FormattedMessage id="modal.quickSearch.openDialog"/>
              : <FormattedMessage id="addToContacts"/>
          }
          </button>
        </div>
      </li>
    );
  }

  handleClick = (event) => {
    const { onSelect, id, isContact } = this.props;
    event.preventDefault();
    onSelect && onSelect(id, isContact)
  };
}

export default ContactItem;
