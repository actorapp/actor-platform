/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { shouldComponentUpdate } from 'react-addons-pure-render-mixin';

import GroupMember from '../activity/GroupMember.react';

class GroupProfileMembers extends Component {
  static propTypes = {
    groupId: PropTypes.number,
    members: PropTypes.array.isRequired
  };

  constructor(props) {
    super(props);

    this.shouldComponentUpdate = shouldComponentUpdate.bind(this);
  }

  renderMembers() {
    const { groupId, members } = this.props;

    return members.map((member) => (
      <GroupMember {...member} gid={groupId} key={member.peerInfo.peer.key} />
    ));
  }

  render() {
    return (
      <ul className="group_profile__members__list">
        {this.renderMembers()}
      </ul>
    );
  }
}

export default GroupProfileMembers;
