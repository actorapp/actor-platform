/*
 * Copyright (C) 2016 Actor LLC. <https://actor.im>
 */

import { map } from 'lodash';
import React, { Component, PropTypes } from 'react';
import { Container } from 'flux/utils';
import classnames from 'classnames';
import { Link } from 'react-router';

import ArchiveActionCreators from '../actions/ArchiveActionCreators';

import ArchiveStore from '../stores/ArchiveStore';

import AvatarItem from './common/AvatarItem.react';
import ConnectionState from './common/ConnectionState.react';
import Scrollbar from './common/Scrollbar.react';

class Archive extends Component {
  constructor(props){
    super(props);
  }

  static getStores = () => [ArchiveStore];

  static calculateState() {
    return {
      isLoading: ArchiveStore.isArchiveLoading(),
      dialogs: ArchiveStore.getDialogs()
    }
  };

  static contextTypes = {
    intl: PropTypes.object
  };

  componentWillMount() {
    ArchiveActionCreators.loadArchivedDialogs();
  }

  render() {
    const { isLoading, dialogs } = this.state;

    const archiveClassname = classnames('archive-section', {
      'archive-section--loading': isLoading
    });

    const dialogsList = map(dialogs, (dialog, index) => {
      const { counter, peer } = dialog;

      return (
        <div className="archive-section__list__item col-xs-12 col-sm-6 col-md-4" key={index}>
          <Link to={`/im/${peer.peer.key}`} className="archive-item row">
            <div className="archive-item__avatar">
              <AvatarItem image={peer.avatar}
                          placeholder={peer.placeholder}
                          size="medium"
                          title={peer.title}/>
              {
                counter !== 0
                  ? <div className="archive-item__counter">{counter}</div>
                  : null
              }
            </div>
            <div className="col-xs">
              <h4 className="archive-item__title">{peer.title}</h4>
            </div>
          </Link>
        </div>
      )
    });

    return (
      <section className="main">
        <header className="toolbar row">
          <h3>Archive</h3>
        </header>
        <ConnectionState/>
        <div className="flexrow">
          <section className={archiveClassname}>
            <Scrollbar>
              <div className="archive-section__list row">
                {
                  dialogs.length !== 0
                    ? dialogsList
                    : !isLoading
                        ? <div className="archive-section__list__item archive-section__list__item--empty col-xs-12">
                            <h3>No dialogs in archive</h3>
                          </div>
                        : null
                }
                {
                  isLoading
                    ? <div className="archive-section__list__item archive-section__list__item--loading col-xs-12">
                        <div className="preloader"><div/><div/><div/><div/><div/></div>
                      </div>
                    : null
                }
              </div>
            </Scrollbar>
          </section>
        </div>
      </section>
    );
  }
}

export default Container.create(Archive, {pure: false});
