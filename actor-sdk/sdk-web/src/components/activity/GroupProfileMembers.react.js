/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import { map } from 'lodash';
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

  render() {
    const { groupId, members } = this.props;

    const membersList = map(members, (member, index) => <GroupMember {...member} gid={groupId} key={index}/>);

    return (
        <ul className="group_profile__members__list">
          {membersList}
        </ul>
    );
  }
}

export default GroupProfileMembers;
