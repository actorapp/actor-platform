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
import Stateful from '../../common/Stateful';

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

    setTimeout(() => {
      const { inviteUserState } = this.state;
      if (inviteUserState === AsyncActionStates.SUCCESS || inviteUserState === AsyncActionStates.FAILURE) {
        InviteUserStore.removeChangeListener(this.onChange);
      }
    }, 0);
  };

  render() {
    const { contact, isMember } = this.props;
    const { inviteUserState } = this.state;

    const contactClassName = classnames('contacts__list__item row', {
      'contacts__list__item--member': isMember
    });

    const controls = isMember
      ? <i className="material-icons">check</i>
      : <Stateful.Root currentState={inviteUserState}>
          <Stateful.Pending>
            <a className="material-icons" onClick={this.onSelect}>person_add</a>
          </Stateful.Pending>
          <Stateful.Processing>
            <i className="material-icons spin">autorenew</i>
          </Stateful.Processing>
          <Stateful.Success>
            <i className="material-icons">check</i>
          </Stateful.Success>
          <Stateful.Failure>
            <i className="material-icons">warning</i>
          </Stateful.Failure>
        </Stateful.Root>;

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
          {controls}
        </div>
      </li>
    );
  }
}

ReactMixin.onClass(ContactItem, PureRenderMixin);

export default ContactItem;
