/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import _ from 'lodash';

import React from 'react';
import ReactMixin from 'react-mixin';
import addons from 'react/addons';

import GroupMember from 'components/activity/GroupMember.react';

const {addons: { PureRenderMixin }} = addons;

@ReactMixin.decorate(PureRenderMixin)
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

export default GroupProfileMembers;
