import React from 'react';
import ReactMixin from 'react-mixin';
import { IntlMixin } from 'react-intl';
import classnames from 'classnames';
import { escapeWithEmoji } from 'utils/EmojiUtils';

import ActivityActionCreators from 'actions/ActivityActionCreators';

import DialogStore from 'stores/DialogStore';
import ActivityStore from 'stores/ActivityStore';

const getStateFromStores = () => {
  return {
    dialogInfo: DialogStore.getSelectedDialogInfo(),
    isActivityOpen: ActivityStore.isOpen()
  };
};

@ReactMixin.decorate(IntlMixin)
class ToolbarSection extends React.Component {
  state = {
    dialogInfo: null,
    isActivityOpen: false
  };

  constructor(props) {
    super(props);

    DialogStore.addSelectedChangeListener(this.onChange);
    ActivityStore.addChangeListener(this.onChange);
  }

  componentWillUnmount() {
    DialogStore.removeSelectedChangeListener(this.onChange);
    ActivityStore.removeChangeListener(this.onChange);
  }

  onClick = () => {
    if (!this.state.isActivityOpen) {
      ActivityActionCreators.show();
    } else {
      ActivityActionCreators.hide();
    }
  };

  onChange = () => this.setState(getStateFromStores());

  render() {
    const { dialogInfo, isActivityOpen } = this.state;

    const infoButtonClassName = classnames('button button--icon', {
      'active': isActivityOpen
    });



    if (dialogInfo !== null) {
      return (
        <header className="toolbar row">
          <div className="toolbar__peer col-xs">
            <span className="toolbar__peer__title" dangerouslySetInnerHTML={{__html: escapeWithEmoji(dialogInfo.name)}}/>
            <span className="toolbar__peer__presence">{dialogInfo.presence}</span>
          </div>

          <div className="toolbar__controls">
            <div className="toolbar__controls__search pull-left hide">
              <i className="material-icons">search</i>
              <input className="input input--search" placeholder={this.getIntlMessage('search')} type="search"/>
            </div>
            <div className="toolbar__controls__buttons pull-right">
              <button className={infoButtonClassName} onClick={this.onClick}>
                <i className="material-icons">info</i>
              </button>
              <button className="button button--icon hide">
                <i className="material-icons">more_vert</i>
              </button>
            </div>
          </div>
        </header>
      );
    } else {
      return (
        <header className="toolbar"/>
      );
    }
  }
}

export default ToolbarSection;
