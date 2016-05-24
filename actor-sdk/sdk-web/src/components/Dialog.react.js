/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import { isFunction } from 'lodash';
import React, { Component, PropTypes } from 'react';
import { Container } from 'flux/utils';
import DelegateContainer from '../utils/DelegateContainer';

import PeerUtils from '../utils/PeerUtils';
import history from '../utils/history';

import DefaultMessages from './dialog/MessagesSection.react';
import DefaultDialogHeader from './dialog/DialogHeader.react';
import DefaultDialogFooter from './dialog/DialogFooter.react';
import DefaultActivity from './Activity.react';
import DefaultCall from './Call.react';
import DialogSearch from './search/DialogSearch.react';
import SearchResults from './search/SearchResults.react';

import UserStore from '../stores/UserStore';
import DialogStore from '../stores/DialogStore';
import DialogInfoStore from '../stores/DialogInfoStore';
import ActivityStore from '../stores/ActivityStore';
import OnlineStore from '../stores/OnlineStore';
import CallStore from '../stores/CallStore';
import SearchMessagesStore from '../stores/SearchMessagesStore'

import DialogActionCreators from '../actions/DialogActionCreators';
import MessageActionCreators from '../actions/MessageActionCreators';
import BlockedUsersActionCreators from '../actions/BlockedUsersActionCreators';
import SearchMessagesActionCreators from '../actions/SearchMessagesActionCreators';

class Dialog extends Component {
  static propTypes = {
    params: PropTypes.shape({
      id: PropTypes.string.isRequired
    }).isRequired
  };

  static getStores() {
    return [ActivityStore, DialogStore, DialogInfoStore, OnlineStore, CallStore, SearchMessagesStore];
  }

  static calculateState() {
    const peer = DialogStore.getCurrentPeer();
    const dialogInfo = DialogInfoStore.getState();

    return {
      peer,
      dialogInfo,
      uid: UserStore.getMyId(),
      isMember: DialogStore.isMember(),
      isActivityOpen: ActivityStore.isOpen(),
      message: OnlineStore.getMessage(),
      isFavorite: DialogStore.isFavorite(peer.id),
      call: Dialog.calculateCallState(peer),
      search: SearchMessagesStore.getState()
    };
  }

  static calculateCallState(peer) {
    const call = CallStore.getState();

    if (!call.isOpen || !PeerUtils.equals(peer, call.peer)) {
      return {
        isCalling: false
      };
    }

    return {
      ...call,
      isCalling: true
    };
  }

  constructor(props, context) {
    super(props, context);
    this.updatePeer(this.props.params.id);

    this.handleStartClick = this.handleStartClick.bind(this);
    this.handleUnblock = this.handleUnblock.bind(this);
    this.handleDialogSearchCancel = this.handleDialogSearchCancel.bind(this);
    this.handleDialogSearchChange = this.handleDialogSearchChange.bind(this);

    this.components = this.getComponents();
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps.params.id !== this.props.params.id) {
      this.updatePeer(nextProps.params.id);
    }
  }

  componentWillUnmount() {
    DialogActionCreators.selectDialogPeer(null);
  }

  updatePeer(id) {
    const peer = PeerUtils.stringToPeer(id);
    if (PeerUtils.hasPeer(peer)) {
      DialogActionCreators.selectDialogPeer(peer);
    } else {
      history.replace('/im');
    }
  }

  handleStartClick() {
    const { peer } = this.state;
    MessageActionCreators.sendTextMessage(peer, '/start');
  }

  handleUnblock() {
    const { dialogInfo } = this.state;
    BlockedUsersActionCreators.unblockUser(dialogInfo.id);
  }

  handleDialogSearchChange(query) {
    SearchMessagesActionCreators.setQuery(query);
  }

  handleDialogSearchCancel() {
    SearchMessagesActionCreators.close();
  }

  getActivityComponents() {
    const { features, components } = DelegateContainer.get();
    const { dialog } = components;

    if (dialog && dialog.activity) {
      return dialog.activity;
    }

    const activity = [DefaultActivity];
    if (features.calls) {
      activity.push(DefaultCall);
    }

    return activity;
  }

  getComponents() {
    const { components: { dialog } } = DelegateContainer.get();
    const activity = this.getActivityComponents();

    if (dialog && !isFunction(dialog)) {
      return {
        activity,
        DialogHeader: isFunction(dialog.header) ? dialog.header : DefaultDialogHeader,
        MessagesSection: isFunction(dialog.messages) ? dialog.messages : DefaultMessages,
        DialogFooter: isFunction(dialog.footer) ? dialog.footer : DefaultDialogFooter
      };
    }

    return {
      activity,
      DialogHeader: DefaultDialogHeader,
      MessagesSection: DefaultMessages,
      DialogFooter: DefaultDialogFooter
    };
  }

  renderActivities() {
    const { activity } = this.components;
    return activity.map((Activity, index) => <Activity key={index} />)
  }

  renderDialogSearch() {
    const { search } = this.state;

    return (
      <DialogSearch
        isOpen={search.isOpen}
        query={search.query}
        onCancel={this.handleDialogSearchCancel}
        onChange={this.handleDialogSearchChange}
      />
    )
  }

  renderContent() {
    const { uid, peer, isMember, dialogInfo, search } = this.state;
    const { MessagesSection, DialogFooter } = this.components;

    if (search.isOpen) {
      return (
        <SearchResults
          query={search.query}
          results={search.results}
          isSearching={search.isSearching}
        />
      );
    }

    return (
      <div className="chat">
        <MessagesSection
          uid={uid}
          peer={peer}
          isMember={isMember}
        />
        <DialogFooter
          info={dialogInfo}
          isMember={isMember}
          onUnblock={this.handleUnblock}
          onStart={this.handleStartClick}
        />
      </div>
    );
  }

  render() {
    const { peer, dialogInfo, message, isFavorite, call, isActivityOpen, search } = this.state;
    const { DialogHeader } = this.components;

    if (!peer) {
      return <section className="main" />;
    }

    return (
      <section className="main">
        <DialogHeader
          info={dialogInfo}
          message={message}
          call={call}
          peer={peer}
          isFavorite={isFavorite}
          isDialogSearchOpen={search.isOpen}
          isActivityOpen={isActivityOpen}
        />
        {this.renderDialogSearch()}
        <div className="flexrow">
          <section className="dialog">
            {this.renderContent()}
          </section>
          {this.renderActivities()}
        </div>
      </section>
    );
  }
}

export default Container.create(Dialog, { withProps: true });
