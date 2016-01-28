/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import _ from 'lodash';

import React from 'react';
import ReactMixin from 'react-mixin';
import PureRenderMixin from 'react-addons-pure-render-mixin';

import GroupMember from '../activity/GroupMember.react';

class GroupProfileMembers extends React.Component {
  static propTypes = {
    groupId: React.PropTypes.number,
    members: React.PropTypes.array.isRequired
  };

  constructor(props) {
    super(props);
  }

  render() {
    const { groupId, members } = this.props;

    const membersList = _.map(members, (member, index) => {
      return <GroupMember {...member} gid={groupId} key={index}/>;
    }, this);

    return (
        <ul className="group_profile__members__list">
          {membersList}
        </ul>
    );
  }
}

ReactMixin.onClass(GroupProfileMembers, PureRenderMixin);

export default GroupProfileMembers;
