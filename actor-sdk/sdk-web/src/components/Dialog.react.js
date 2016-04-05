/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import { isFunction } from 'lodash';

import React, { Component, PropTypes } from 'react';
import { Container } from 'flux/utils';

import PeerUtils from '../utils/PeerUtils';

import DefaultMessages from './dialog/MessagesSection.react';
import DialogFooter from './dialog/DialogFooter.react';
import DefaultToolbar from './Toolbar.react';
import DefaultActivity from './Activity.react';
import DefaultSearch from './search/SearchSection.react';
import DefaultCall from './Call.react';
import ConnectionState from './common/ConnectionState.react';

import ActivityStore from '../stores/ActivityStore';
import DialogStore from '../stores/DialogStore';

import DialogActionCreators from '../actions/DialogActionCreators';

class DialogSection extends Component {
  static contextTypes = {
    router: PropTypes.object.isRequired,
    delegate: PropTypes.object.isRequired
  };

  static propTypes = {
    params: PropTypes.shape({
      id: PropTypes.string.isRequired
    }).isRequired
  };

  static getStores() {
    return [ActivityStore, DialogStore];
  }

  static calculateState() {
    return {
      peer: DialogStore.getCurrentPeer(),
      isMember: DialogStore.isMember(),
      isActivityOpen: ActivityStore.isOpen()
    };
  }

  constructor(props, context) {
    super(props, context);
    this.updatePeer(this.props.params.id);
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
      this.context.router.replace('/im');
    }
  }

  getActivityComponents() {
    const { features, components: { dialog } } = this.context.delegate;
    if (dialog && dialog.activity) {
      return dialog.activity;
    }

    const activity = [DefaultActivity];
    if (features.call) {
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
    const { peer, isMember } = this.state;
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
            <MessagesSection peer={peer} isMember={isMember} />
            <DialogFooter isMember={isMember} />
          </section>
          {activity.map((Activity, index) => <Activity key={index} />)}
        </div>
      </section>
    );
  }
}

export default Container.create(DialogSection, {withProps: true});
