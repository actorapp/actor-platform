import React from 'react';
import classnames from 'classnames';

import DialogStore from 'stores/DialogStore';

import ActivityActionCreators from 'actions/ActivityActionCreators';
import ActivityStore from 'stores/ActivityStore';

//import AvatarItem from 'components/common/AvatarItem.react';

const getStateFromStores = () => {
  return {
    dialogInfo: DialogStore.getSelectedDialogInfo(),
    isActivityOpen: ActivityStore.isOpen()
  };
};

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

  onChange = () => {
    this.setState(getStateFromStores());
  };

  render() {
    const info = this.state.dialogInfo;
    const isActivityOpen = this.state.isActivityOpen;

    let infoButtonClassName = classnames('button button--icon', {
      'button--active': isActivityOpen
    });

    if (info != null) {
      return (
        <header className="toolbar">
          <div className="pull-left">
            <div className="toolbar__peer row">
              <div className="toolbar__peer__body col-xs">
                <span className="toolbar__peer__title">{info.name}</span>
                <span className="toolbar__peer__presence">{info.presence}</span>
              </div>
            </div>
          </div>

          <div className="toolbar__controls pull-right">
            <div className="toolbar__controls__search pull-left hide">
              <i className="material-icons">search</i>
              <input className="input input--search" placeholder="Search" type="search"/>
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
        <header className="toolbar">
        </header>
      );
    }
  }
}

export default ToolbarSection;
