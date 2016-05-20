/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import EventListener from 'fbjs/lib/EventListener';
import { findDOMNode } from 'react-dom';
import { shouldComponentUpdate } from 'react-addons-pure-render-mixin';
import { FormattedMessage } from 'react-intl';
import isInside from '../../../utils/isInside';
import confirm from '../../../utils/confirm';

import DialogStore from '../../../stores/DialogStore';

import ArchiveActionCreators from '../../../actions/ArchiveActionCreators';
import FavoriteActionCreators from '../../../actions/FavoriteActionCreators';
import DropdownActionCreators from '../../../actions/DropdownActionCreators';
import DialogActionCreators from '../../../actions/DialogActionCreators';

class RecentContextMenu extends Component {
  static propTypes = {
    peer: PropTypes.object.isRequired,
    contextPos: PropTypes.object.isRequired
  };

  static contextTypes = {
    intl: PropTypes.object
  };

  constructor(props) {
    super(props);

    this.shouldComponentUpdate = shouldComponentUpdate.bind(this);
  }

  componentDidMount() {
    this.listeners = [
      EventListener.capture(document, 'click', this.handleDocumentClick),
      EventListener.capture(document, 'contextmenu', this.handleClose),
      EventListener.capture(document, 'scroll', this.handleClose)
    ];
  }

  componentWillUnmount() {
    this.listeners.forEach((listener) => {
      listener.remove();
    });

    this.listeners = null;
  }

  handleDocumentClick = (event) => {
    const menu = findDOMNode(this.refs.menu);
    const menuRect = menu.getBoundingClientRect();
    const coords = {
      x: event.pageX || event.clientX,
      y: event.pageY || event.clientY
    };

    if (!isInside(coords, menuRect)) {
      // event.preventDefault();
      this.handleClose();
    }
  };

  handleClose = () => DropdownActionCreators.hideRecentContext();

  handleAddToArchive = () => {
    const { peer } = this.props;
    ArchiveActionCreators.archiveChat(peer);
    this.handleClose();
  };

  handleFavorite = () => {
    const { peer } = this.props;
    FavoriteActionCreators.favoriteChat(peer);
    this.handleClose();
  };

  handleUnfavorite = () => {
    const { peer } = this.props;
    FavoriteActionCreators.unfavoriteChat(peer);
    this.handleClose();
  };

  handleDelete = () => {
    const { peer } = this.props;
    confirm(<FormattedMessage id="modal.confirm.delete"/>).then(
      () => DialogActionCreators.deleteChat(peer),
      () => {}
    );
  };

  render() {
    const { peer, contextPos } = this.props;
    const isFavorite = DialogStore.isFavorite(peer.id);

    const dropdownStyles = {
      top: contextPos.y,
      left: contextPos.x
    };

    const dropdownMenuStyles = {
      minWidth: 140,
      left: 2,
      top: 2
    };

    return (
      <div className="dropdown dropdown--opened dropdown--small" style={dropdownStyles}>
        <ul className="dropdown__menu dropdown__menu--left" ref="menu" style={dropdownMenuStyles}>
          {
            isFavorite
              ? <li className="dropdown__menu__item" onClick={this.handleUnfavorite}>
                  <i className="icon material-icons">star_border</i> <FormattedMessage id="context.favorite.remove"/>
                </li>
              : <li className="dropdown__menu__item" onClick={this.handleFavorite}>
                  <i className="icon material-icons">star</i> <FormattedMessage id="context.favorite.add"/>
                </li>
          }
          <li className="dropdown__menu__item" onClick={this.handleAddToArchive}>
            <i className="icon material-icons">archive</i> <FormattedMessage id="context.archive"/>
          </li>
          <li className="dropdown__menu__item" onClick={this.handleDelete}>
            <i className="icon material-icons">delete</i> <FormattedMessage id="context.delete"/>
          </li>
        </ul>
      </div>
    );
  }
}

export default RecentContextMenu;
