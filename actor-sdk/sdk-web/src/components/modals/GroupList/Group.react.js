/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import ReactMixin from 'react-mixin';
import { IntlMixin } from 'react-intl';

import GroupListActionCreators from '../../../actions/GroupListActionCreators'

import GroupListStore from '../../../stores/GroupListStore';

import AvatarItem from '../../common/AvatarItem.react';

import { escapeWithEmoji } from '../../../utils/EmojiUtils';

class Group extends Component {
  constructor(props) {
    super(props);
  }

  static propTypes = {
    group: PropTypes.object.isRequired,
    onClick: PropTypes.func.isRequired
  };

  handleClick = () => {
    const { group, onClick } = this.props;
    onClick(group.peerInfo.peer);
  };

  render() {
    const { group } = this.props;

    return (
      <li className="group__list__item row" onClick={this.handleClick}>
        <div>
          <AvatarItem image={group.peerInfo.avatar}
                      placeholder={group.peerInfo.placeholder}
                      size="medium"
                      title={group.peerInfo.title}/>
          {
            group.isPublic
              ? <i className="material-icons">public</i>
              : null
          }
        </div>

        <div className="col-xs">
          <div className="meta">
            <span className="title" dangerouslySetInnerHTML={{__html: escapeWithEmoji(group.peerInfo.title)}}/>
            {
              group.isJoined
                ? <span className="join-status">Joined</span>
                : null
            }
            {
              group.description
                ? <span className="description" dangerouslySetInnerHTML={{__html: escapeWithEmoji(group.description)}}/>
                : null
            }
          </div>
        </div>

        <div className="additional">
          <div className="members">
            <svg className="icon"
                 dangerouslySetInnerHTML={{__html: '<use xlink:href="assets/images/icons.svg#members"/>'}}/>
            {group.membersCount}
          </div>
        </div>
      </li>
    )
  }
}

ReactMixin.onClass(Group, IntlMixin);

export default Group;
