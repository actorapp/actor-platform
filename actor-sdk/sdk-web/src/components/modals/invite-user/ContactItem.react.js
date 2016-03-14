/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import ReactMixin from 'react-mixin';
import PureRenderMixin from 'react-addons-pure-render-mixin';
import classnames from 'classnames';

import InviteUserStore from '../../../stores/InviteUserStore';

import { AsyncActionStates } from '../../../constants/ActorAppConstants';

import AvatarItem from '../../common/AvatarItem.react';
import Stateful from '../../common/Stateful.react';

import { escapeWithEmoji } from '../../../utils/EmojiUtils';

const getStateFromStore = (props) => {
  const { contact } = props;

  return {
    inviteUserState: InviteUserStore.getInviteUserState(contact.uid)
  }
};

class ContactItem extends Component {
  static propTypes = {
    contact: PropTypes.object,
    onSelect: PropTypes.func,
    isMember: PropTypes.bool
  };

  constructor(props) {
    super(props);

    this.state = getStateFromStore(props);
  }

  componentWillUnmount() {
    const { contact } = this.props;
    InviteUserStore.resetInviteUserState(contact.uid);
  }

  onSelect = () => {
    const { contact, onSelect } = this.props;

    InviteUserStore.addChangeListener(this.onChange);
    onSelect(contact);
  };

  onChange = () => {
    this.setState(getStateFromStore(this.props));

    setImmediate(() => {
      const { inviteUserState } = this.state;
      if (inviteUserState === AsyncActionStates.SUCCESS || inviteUserState === AsyncActionStates.FAILURE) {
        InviteUserStore.removeChangeListener(this.onChange);
      }
    });
  };

  getControls() {
    const { isMember } = this.props;
    if (isMember) return <i className="material-icons">check</i>;

    const { inviteUserState } = this.state;
    return (
      <Stateful
        currentState={inviteUserState}
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
          <span className="title" dangerouslySetInnerHTML={{__html: escapeWithEmoji(contact.name)}}/>
        </div>

        <div className="controls">
          {this.getControls()}
        </div>

      </li>
    );
  }
}

ReactMixin.onClass(ContactItem, PureRenderMixin);

export default ContactItem;
