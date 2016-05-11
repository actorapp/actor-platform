/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import { isFunction } from 'lodash';

import React, { Component, PropTypes } from 'react';
import { Container } from 'flux/utils';

import PeerUtils from '../utils/PeerUtils';
import history from '../utils/history';

import DefaultMessages from './dialog/MessagesSection.react';
import DialogFooter from './dialog/DialogFooter.react';
import DefaultToolbar from './Toolbar.react';
import DefaultActivity from './Activity.react';
import DefaultSearch from './search/SearchSection.react';
import DefaultCall from './Call.react';
import ConnectionState from './common/ConnectionState.react';

import UserStore from '../stores/UserStore';
import DialogStore from '../stores/DialogStore';
import DialogInfoStore from '../stores/DialogInfoStore';
import ActivityStore from '../stores/ActivityStore';

import DialogActionCreators from '../actions/DialogActionCreators';
import MessageActionCreators from '../actions/MessageActionCreators';
import BlockedUsersActionCreators from '../actions/BlockedUsersActionCreators';

class DialogSection extends Component {
  static contextTypes = {
    delegate: PropTypes.object.isRequired
  };

  static propTypes = {
    params: PropTypes.shape({
      id: PropTypes.string.isRequired
    }).isRequired
  };

  static getStores() {
    return [ActivityStore, DialogStore, DialogInfoStore];
  }

  static calculateState() {
    const peer = DialogStore.getCurrentPeer();
    const dialogInfo = DialogInfoStore.getState();

    return {
      peer,
      dialogInfo,
      uid: UserStore.getMyId(),
      isMember: DialogStore.isMember(),
      isActivityOpen: ActivityStore.isOpen()
    };
  }

  constructor(props, context) {
    super(props, context);
    this.updatePeer(this.props.params.id);

    this.onStart = this.onStart.bind(this);
    this.onUnblock = this.onUnblock.bind(this);
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

  onStart() {
    const { peer } = this.state;
    MessageActionCreators.sendTextMessage(peer, '/start');
  }

  onUnblock() {
    const { dialogInfo } = this.state;
    BlockedUsersActionCreators.unblockUser(dialogInfo.id);
  }

  getActivityComponents() {
    const { features, components: { dialog } } = this.context.delegate;
    if (dialog && dialog.activity) {
      return dialog.activity;
    }

    const activity = [DefaultActivity];
    if (features.calls) {
      activity.push(DefaultCall);
    }

    if (features.search) {
      activity.push(DefaultSearch);
    }

    return activity;
  }

  getComponents() {
    const { dialog } = this.context.delegate.components;
    const activity = this.getActivityComponents();

    if (dialog && !isFunction(dialog)) {
      return {
        activity,
        ToolbarSection: dialog.toolbar || DefaultToolbar,
        MessagesSection: isFunction(dialog.messages) ? dialog.messages : DefaultMessages
      };
    }

    return {
      activity,
      ToolbarSection: DefaultToolbar,
      MessagesSection: DefaultMessages
    };
  }

  render() {
    const { uid, peer, isMember, dialogInfo } = this.state;
    if (!peer) {
      return <section className="main" />;
    }

    const {
      ToolbarSection,
      MessagesSection,
      activity
    } = this.getComponents();

    return (
      <section className="main">
        <ToolbarSection />
        <div className="flexrow">
          <section className="dialog">
            <ConnectionState/>
            <div className="chat">
              <MessagesSection uid={uid} peer={peer} isMember={isMember} />
              <DialogFooter
                info={dialogInfo}
                isMember={isMember}
                onUnblock={this.onUnblock}
                onStart={this.onStart}
              />
            </div>
          </section>
          {activity.map((Activity, index) => <Activity key={index} />)}
        </div>
      </section>
    );
  }
}

export default Container.create(DialogSection, { withProps: true });
