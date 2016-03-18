/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import ReactMixin from 'react-mixin';
import PureRenderMixin from 'react-addons-pure-render-mixin';
import classnames from 'classnames';
import { AsyncActionStates } from '../../../constants/ActorAppConstants';
import AvatarItem from '../../common/AvatarItem.react';
import Stateful from '../../common/Stateful.react';

import { escapeWithEmoji } from '../../../utils/EmojiUtils';

class ContactItem extends Component {
  static propTypes = {
    contact: PropTypes.object.isRequired,
    onSelect: PropTypes.func,
    isMember: PropTypes.bool,
    inviteState: PropTypes.oneOf([
      AsyncActionStates.PENDING,
      AsyncActionStates.PROCESSING,
      AsyncActionStates.SUCCESS,
      AsyncActionStates.FAILURE
    ])
  };

  static defaultProps = {
    inviteState: AsyncActionStates.PENDING
  };

  onSelect = () => {
    const { contact, onSelect } = this.props;
    onSelect && onSelect(contact);
  };

  renderTitle() {
    const { contact } = this.props;
    return (
      <span className="title" dangerouslySetInnerHTML={{__html: escapeWithEmoji(contact.name)}}/>
    );
  }

  renderControls() {
    const { isMember, inviteState } = this.props;
    if (isMember) return <i className="material-icons">check</i>;

    return (
      <Stateful
        currentState={inviteState}
        pending={<a className="material-icons" onClick={this.onSelect}>person_add</a>}
        processing={<i className="material-icons spin">autorenew</i>}
        success={<i className="material-icons">check</i>}
        failure={<i className="material-icons">warning</i>}
      />
    );
  }

  render() {
    const { contact, isMember } = this.props;

    const contactClassName = classnames('contacts__list__item row', {
      'contacts__list__item--member': isMember
    });

    return (
      <li className={contactClassName}>
        <AvatarItem image={contact.avatar}
                    placeholder={contact.placeholder}
                    size="small"
                    title={contact.name}/>

        <div className="col-xs">
          {this.renderTitle()}
        </div>

        <div className="controls">
          {this.renderControls()}
        </div>

      </li>
    );
  }
}

ReactMixin.onClass(ContactItem, PureRenderMixin);

export default ContactItem;
