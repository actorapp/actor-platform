/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import ReactMixin from 'react-mixin';
import addons from 'react/addons';
import classnames from 'classnames';

import AvatarItem from '../../common/AvatarItem.react';

import { escapeWithEmoji } from '../../../utils/EmojiUtils';

const {addons: { PureRenderMixin }} = addons;

class PeopleItem extends Component {
  constructor(props) {
    super(props);
  }

  static propTypes = {
    contact: PropTypes.object.isRequired,
    isSelected: PropTypes.bool.isRequired,
    onClick: PropTypes.func.isRequired,
    onMouseOver: PropTypes.func.isRequired
  };

  handleClick = () => {
    const { contact, onClick } = this.props;
    onClick(contact);
  };

  handleMouseOver= () => {
    const { onMouseOver } = this.props;
    onMouseOver();
  };


  render() {
    const { contact, isSelected } = this.props;
    const resultClassName = classnames('contacts__list__item row', {
      'contacts__list__item--active': isSelected
    });

    return (
      <li className={resultClassName}
          onClick={this.handleClick}
          onMouseOver={this.handleMouseOver}>
        <AvatarItem image={contact.avatar}
                    placeholder={contact.placeholder}
                    size="medium"
                    title={contact.name}/>

        <div className="col-xs">
          <span className="title" dangerouslySetInnerHTML={{__html: escapeWithEmoji(contact.name)}}/>
        </div>
      </li>
    );
  }
}

ReactMixin.onClass(PeopleItem, PureRenderMixin);

export default PeopleItem;
