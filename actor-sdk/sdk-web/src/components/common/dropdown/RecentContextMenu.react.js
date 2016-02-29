/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { findDOMNode } from 'react-dom';
import ReactMixin from 'react-mixin';
import PureRenderMixin from 'react-addons-pure-render-mixin';
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
    contextPos: PropTypes.object.isRequired,
    hideOnScroll: PropTypes.bool.isRequired
  };

  static contextTypes = {
    intl: PropTypes.object
  };

  constructor(props) {
    super(props);

    document.addEventListener('click', this.handleDocumentClick, true);
    document.addEventListener('contextmenu', this.handleClose, true);

    if (props.hideOnScroll) {
      document.addEventListener('scroll', this.handleClose, true);
    }
  }

  componentWillUnmount() {
    const { hideOnScroll } = this.props;
    document.removeEventListener('click', this.handleDocumentClick, true);
    document.removeEventListener('contextmenu', this.handleClose, true);

    if (hideOnScroll) {
      document.removeEventListener('scroll', this.handleClose, true);
    }
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

  handleAddToArchive = (event) => {
    const { peer } = this.props;
    ArchiveActionCreators.archiveChat(peer);
    this.handleClose();
  };

  handleFavorite = (event) => {
    const { peer } = this.props;
    FavoriteActionCreators.favoriteChat(peer);
    this.handleClose();
  };

  handleUnfavorite = (event) => {
    const { peer } = this.props;
    FavoriteActionCreators.unfavoriteChat(peer);
    this.handleClose();
  };

  handleDelete = (event) => {
    const { intl } = this.context;
    const { peer } = this.props;
    confirm(intl.messages['modal.confirm.delete']).then(
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
                  <i className="icon material-icons">star_border</i> Unfavorite
                </li>
              : <li className="dropdown__menu__item" onClick={this.handleFavorite}>
                  <i className="icon material-icons">star</i> Favorite
                </li>
          }
          <li className="dropdown__menu__item" onClick={this.handleAddToArchive}>
            <i className="icon material-icons">archive</i> Send to archive
          </li>
          <li className="dropdown__menu__item" onClick={this.handleDelete}>
            <i className="icon material-icons">delete</i> Delete
          </li>
        </ul>
      </div>
    );
  }
}

ReactMixin.onClass(RecentContextMenu, PureRenderMixin);

export default RecentContextMenu;
