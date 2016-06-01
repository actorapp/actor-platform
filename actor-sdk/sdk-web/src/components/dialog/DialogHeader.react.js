/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { FormattedMessage } from 'react-intl';
import classnames from 'classnames';
import Tooltip from 'rc-tooltip';
import { PeerTypes } from '../../constants/ActorAppConstants';

import { escapeWithEmoji } from '../../utils/EmojiUtils';

import CallActionCreators from '../../actions/CallActionCreators';
import ActivityActionCreators from '../../actions/ActivityActionCreators';
import FavoriteActionCreators from '../../actions/FavoriteActionCreators';
import SearchMessagesActionCreators from '../../actions/SearchMessagesActionCreators';
import InviteUserActions from '../../actions/InviteUserActions';

import AvatarItem from '../common/AvatarItem.react';
import ToggleFavorite from '../common/ToggleFavorite.react';
import MoreDropdown from './header/MoreDropdown.react';
import SmartCallButton from '../call/SmartCallButton.react';

const MAX_GROUP_CALL_SIZE = 25;

class DialogHeader extends Component {
  static contextTypes = {
    delegate: PropTypes.object.isRequired
  }

  static propTypes = {
    peer: PropTypes.object.isRequired,
    info: PropTypes.object.isRequired,
    call: PropTypes.object.isRequired,
    message: PropTypes.string,
    isFavorite: PropTypes.bool.isRequired,
    isActivityOpen: PropTypes.bool.isRequired,
    isDialogSearchOpen: PropTypes.bool.isRequired
  }

  constructor(props, context) {
    super(props, context);

    this.state = {
      isMoreDropdownOpen: false
    }

    this.onFavoriteToggle = this.onFavoriteToggle.bind(this);
    this.handleInfoButtonClick = this.handleInfoButtonClick.bind(this);
    this.handleMakeCallButtonClick = this.handleMakeCallButtonClick.bind(this);
    this.handleEndCallButtonClick = this.handleEndCallButtonClick.bind(this);
    this.handleSearchButtonClick = this.handleSearchButtonClick.bind(this);
    this.toggelMoreDropdownOpen = this.toggelMoreDropdownOpen.bind(this);
    this.handleAddPeople = this.handleAddPeople.bind(this);
  }

  onFavoriteToggle() {
    const { peer, isFavorite } = this.props;

    if (isFavorite) {
      FavoriteActionCreators.unfavoriteChat(peer);
    } else {
      FavoriteActionCreators.favoriteChat(peer);
    }
  }

  handleInfoButtonClick() {
    const { call, isActivityOpen } = this.props;
    if (call.isCalling) {
      CallActionCreators.toggleFloating();
    } else if (isActivityOpen) {
      ActivityActionCreators.hide();
    } else {
      ActivityActionCreators.show();
    }
  }

  handleMakeCallButtonClick() {
    const { peer } = this.props;
    CallActionCreators.makePeerCall(peer);
  }

  handleEndCallButtonClick() {
    const { call } = this.props;
    CallActionCreators.endCall(call.id);
  }

  handleSearchButtonClick() {
    const { isDialogSearchOpen } = this.props;
    if (!isDialogSearchOpen) {
      SearchMessagesActionCreators.open();
    } else {
      SearchMessagesActionCreators.close();
    }
  }

  handleAddPeople() {
    const { info } = this.props;
    InviteUserActions.show(info)
  }

  toggelMoreDropdownOpen() {
    const { isMoreDropdownOpen } = this.state;
    this.setState({ isMoreDropdownOpen: !isMoreDropdownOpen })
  }

  renderMessage() {
    const { message } = this.props;

    return message;
  }

  renderInfoButton() {
    const { call, isActivityOpen } = this.props;

    const className = classnames('button button--icon', {
      'active': isActivityOpen || (call.isCalling && !call.isFloating)
    });

    return (
      <Tooltip
        placement="left"
        mouseEnterDelay={0}
        mouseLeaveDelay={0}
        overlay={<FormattedMessage id="tooltip.toolbar.info"/>}
      >
        <button className={className} onClick={this.handleInfoButtonClick}>
          <i className="material-icons">info</i>
        </button>
      </Tooltip>
    )
  }

  renderVerified() {
    const { info } = this.props;

    if (!info.isVerified) {
      return null;
    }

    return (
      <span className="dialog__header__peer__verified">
        <i className="material-icons">verified_user</i>
      </span>
    );
  }

  renderFavorite() {
    const { isFavorite } = this.props;
    const favoriteClassName = classnames('dialog__header__peer__favorite', {
      'dialog__header__peer__favorite--active': isFavorite
    });

    return (
      <Tooltip
        placement="bottom"
        mouseEnterDelay={0}
        mouseLeaveDelay={0}
        overlay={<FormattedMessage id="tooltip.toolbar.favorite"/>}
      >
        <span className={favoriteClassName}>
          <ToggleFavorite value={isFavorite} onToggle={this.onFavoriteToggle} />
        </span>
      </Tooltip>
    )
  }

  renderSearchButton() {
    const { delegate } = this.context;
    const { isDialogSearchOpen } = this.props;

    if (!delegate.features.search) {
      return null;
    }

    const callButtonClassName = classnames('button button--icon', {
      'active': isDialogSearchOpen
    });

    return (
      <button className={callButtonClassName} onClick={this.handleSearchButtonClick}>
        <i className="material-icons">search</i>
      </button>
    );
  }

  renderCallButton() {
    const { delegate } = this.context;
    const { peer, info } = this.props;

    if (!delegate.features.calls) {
      return null;
    }

    if (peer.type === PeerTypes.GROUP && info.members.length > MAX_GROUP_CALL_SIZE) {
      return null;
    }

    const { call } = this.props;
    return (
      <SmartCallButton
        call={call}
        onCallStart={this.handleMakeCallButtonClick}
        onCallEnd={this.handleEndCallButtonClick}
      />
    );
  }

  renderMoreDropdown() {
    const { isMoreDropdownOpen } = this.state;

    if (!isMoreDropdownOpen) {
      return null;
    }

    const { info, peer } = this.props;

    return (
      <MoreDropdown
        onClose={this.toggelMoreDropdownOpen}
        info={info}
        peer={peer}
      />
    );
  }

  renderMoreButton() {
    const { isMoreDropdownOpen } = this.state;

    const dropdownButtonClassNames = classnames('button button--icon', {
      'active': isMoreDropdownOpen
    })

    return (
      <div className="dropdown dropdown--opened">
        <button className={dropdownButtonClassNames} onClick={this.toggelMoreDropdownOpen}>
          <i className="material-icons">more_vert</i>
        </button>
        {this.renderMoreDropdown()}
      </div>
    );
  }

  renderAddUsersButton() {
    const { peer } = this.props;
    if (peer.type === PeerTypes.USER) {
      return null;
    }

    return (
      <button className="button button--icon" onClick={this.handleAddPeople}>
        <i className="material-icons">group_add</i>
      </button>
    );
  }

  render() {
    const { info } = this.props;

    if (!info) {
      return <header className="dialog__header" />;
    }

    return (
      <header className="dialog__header">
        <div className="row">
          <AvatarItem
            className="dialog__header__avatar"
            size="medium"
            image={info.avatar}
            placeholder={info.placeholder}
            title={info.name}
          />

          <div className="dialog__header__peer">
            <header className="dialog__header__peer__title" >
              <span dangerouslySetInnerHTML={{ __html: escapeWithEmoji(info.name) }}/>
              {this.renderVerified()}
              {this.renderFavorite()}
            </header>
            <div className="dialog__header__peer__message">
              {this.renderMessage()}
            </div>
          </div>

          <div className="col-xs"/>

          <div className="dialog__header__controls">
            {this.renderSearchButton()}
            {this.renderCallButton()}
            {this.renderAddUsersButton()}
            {this.renderInfoButton()}
            {this.renderMoreButton()}
          </div>
        </div>
      </header>
    );
  }
}

export default DialogHeader;
