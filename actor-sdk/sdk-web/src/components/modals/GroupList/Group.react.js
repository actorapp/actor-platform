/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import classnames from 'classnames';

import AvatarItem from '../../common/AvatarItem.react';

import { escapeWithEmoji } from '../../../utils/EmojiUtils';

class Group extends Component {
  constructor(props) {
    super(props);
  }

  static propTypes = {
    group: PropTypes.object.isRequired,
    isSelected: PropTypes.bool.isRequired,
    onClick: PropTypes.func.isRequired,
    onMouseOver: PropTypes.func.isRequired
  };

  handleClick = () => {
    const { group, onClick } = this.props;
    onClick(group.peerInfo.peer);
  };

  handleMouseOver= () => {
    const { onMouseOver } = this.props;
    onMouseOver();
  };

  render() {
    const { group, isSelected } = this.props;
    const resultClassName = classnames('group__list__item row', {
      'group__list__item--active': isSelected
    });

    return (
      <li className={resultClassName}
          onClick={this.handleClick}
          onMouseOver={this.handleMouseOver}>
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

export default Group;
