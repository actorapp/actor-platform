/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { FormattedMessage } from 'react-intl';
import classnames from 'classnames';
import Tooltip from 'rc-tooltip';
import alert from '../../utils/alert';

import { escapeWithEmoji } from '../../utils/EmojiUtils';

import CallActionCreators from '../../actions/CallActionCreators';
import ActivityActionCreators from '../../actions/ActivityActionCreators';
import FavoriteActionCreators from '../../actions/FavoriteActionCreators';
import DialogSearchActionCreators from '../../actions/DialogSearchActionCreators';

import AvatarItem from '../common/AvatarItem.react';
import ToggleFavorite from '../common/ToggleFavorite.react';

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

    this.onFavoriteToggle = this.onFavoriteToggle.bind(this);
    this.handleInfoButtonClick = this.handleInfoButtonClick.bind(this);
    this.handleSearchButtonClick = this.handleSearchButtonClick.bind(this);
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


  handleCallButtonClick() {
    alert('callButtonClick')
      .then(() => console.debug('Alert closed'));
  }

  handleSearchButtonClick() {
    const { isDialogSearchOpen } = this.props;
    if (!isDialogSearchOpen) {
      DialogSearchActionCreators.open();
    } else {
      DialogSearchActionCreators.close();
    }
  }

  renderMessage() {
    const { call, message } = this.props;

    let peerMessage;
    if (call.isCalling) {
      peerMessage = <FormattedMessage id={`call.state.${call.state}`} values={{ time: call.time }} />
    } else {
      peerMessage = message;
    }

    return (
      <div className="dialog__header__peer__message">{peerMessage}</div>
    )
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
    const { isExperimental } = this.context;

    if (!isExperimental) {
      return null;
    }

    const callButtonClassName = classnames('button button--icon');

    return (
      <button className={callButtonClassName} onClick={this.handleCallButtonClick}>
        <i className="material-icons" style={{ fontSize: 22 }}>call</i>
      </button>
    );
  }

  render() {
    const { info } = this.props;

    if (!info) {
      return <header className="dialog__header" />;
    }

    const headerClassName = classnames('dialog__header row');

    return (
      <header className={headerClassName}>
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
          {this.renderMessage()}
        </div>

        <div className="col-xs"/>

        <div className="dialog__header__controls">
          {this.renderSearchButton()}
          {this.renderCallButton()}
          {this.renderInfoButton()}
        </div>
      </header>
    );
  }
}

export default DialogHeader;
