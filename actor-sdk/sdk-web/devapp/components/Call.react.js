/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, { Component } from 'react';

import AvatarItem from '../../src/components/common/AvatarItem.react';

const user = {
  about: 'From this point on, all you opinions will be rejected!',
  avatar: 'https://api.actor.im/v1/files/-4614194743534365985/small-avatar.jpg?signature=7fe0a102f6e3957be07d198e5101fd64540a764a85db905bf049f90fd7c04ebe&expires=1456929067260',
  bigAvatar: 'https://api.actor.im/v1/files/1655515984124292513/large-avatar.jpg?signature=bdb4c5d9e32995e5c355bb8855321fe900b780a9177b17521693c48f4550f4e6&expires=1456930419548',
  emails: [],
  id: 654236281,
  isBot: false,
  isContact: true,
  isOnline: false,
  name: 'Ichigo Kurosaki',
  nick: 'ichi_kun',
  phones: [],
  placeholder: 'green',
  presence: null,
}

export default class Call extends Component {
  constructor(props) {
    super(props);
    console.debug('Call', props);
  }

  render() {
    return (
      <section className="call">
        <header className="call__header">
          <div className="call__avatar">
            <AvatarItem image={user.avatarBig}
                        placeholder={user.placeholder}
                        size="big"
                        title={user.name}/>
            <div className="call__avatar__rings">
              <div/><div/><div/>
            </div>
          </div>
          <h3 className="call__title">{user.name}</h3>
          <div className="call__state">Calling</div>
        </header>
        <footer className="call__footer"></footer>
      </section>
    )
  }
}
