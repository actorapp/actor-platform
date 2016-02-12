/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import { map } from 'lodash';
import React, { Component, PropTypes } from 'react';
import ReactMixin from 'react-mixin';
import PureRenderMixin from 'react-addons-pure-render-mixin';

import GroupMember from '../activity/GroupMember.react';

class GroupProfileMembers extends Component {
  static propTypes = {
    groupId: PropTypes.number,
    members: PropTypes.array.isRequired
  };

  constructor(props) {
    super(props);
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

ReactMixin.onClass(GroupProfileMembers, PureRenderMixin);

export default GroupProfileMembers;
