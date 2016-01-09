/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import ReactMixin from 'react-mixin';
import addons from 'react/addons';
import classnames from 'classnames';

import AvatarItem from '../../common/AvatarItem.react';

import { escapeWithEmoji } from '../../../utils/EmojiUtils';

const {addons: { PureRenderMixin }} = addons;

class ContactItem extends Component {
  static propTypes = {
    id: PropTypes.number.isRequired,
    name: PropTypes.string.isRequired,
    nick: PropTypes.string.isRequired,
    avatar: PropTypes.string.isRequired,
    about: PropTypes.string.isRequired,
    placeholder: PropTypes.string.isRequired,
    emails: PropTypes.array.isRequired,
    phones: PropTypes.array.isRequired,

    isBot: PropTypes.bool.isRequired,
    isContact: PropTypes.bool.isRequired,
    isOnline: PropTypes.bool.isRequired,

    onSelect: React.PropTypes.func
  };

  constructor(props) {
    super(props);
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
              <span dangerouslySetInnerHTML={{__html: escapeWithEmoji(name)}}/>
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
              <div className="title">about</div>
              {about}
            </div>
              : null
          }
          {
            emails[0]
              ? <div className="email">
                  <div className="title">email:</div>
                  <a href={'mailto:' + emails[0].email}>{emails[0].email}</a>
                </div>
              : null
          }
          {
            phones[0]
              ? <div className="email">
                  <div className="title">phone:</div>
                  <a href={'tel:' + phones[0].email}>{phones[0].number}</a>
                </div>
              : null
          }
        </div>
        <div className="controls">
          <button className="button button--rised" onClick={this.handleClick}>
          {
            /* TODO: Need to translate */
            isContact
              ? 'Open conversation'
              : 'Add to contacts'
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

ReactMixin.onClass(ContactItem, PureRenderMixin);

export default ContactItem;
