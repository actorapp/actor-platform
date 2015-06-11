import React from 'react';
import AvatarItem from './common/AvatarItem.react';

import DialogStore from '../stores/DialogStore';

import ActivityActionCreators from '../actions/ActivityActionCreators';

var getStateFromStores = () => {
  return {dialogInfo: null};
};

class ToolbarSection extends React.Component {
  componentWillMount() {
    DialogStore.addSelectedChangeListener(this._onChange);
  }

  componentWillUnmount() {
    DialogStore.removeSelectedChangeListener(this._onChange);
  }

  constructor() {
    super();

    this._onClick = this._onClick.bind(this);
    this._onChange = this._onChange.bind(this);

    this.state = getStateFromStores();
  }

  _onClick() {
    ActivityActionCreators.show();
  }

  _onChange() {
    this.setState({dialogInfo: DialogStore.getSelectedDialogInfo()});
  }

  render() {
    let info = this.state.dialogInfo;
    let dialogElement;

    if (info != null) {
      dialogElement =
        <div className="toolbar__peer row">
          <a onClick={this._onClick}>
            <AvatarItem image={info.avatar}
                        placeholder={info.placeholder}
                        size="small"
                        title={info.name}/>
          </a>
          <div className="toolbar__peer__body col-xs">
            <span className="toolbar__peer__title" onClick={this._onClick}>{info.name}</span>
            <span className="toolbar__peer__presence">{info.presence}</span>
          </div>
        </div>;
    } else {
      dialogElement = null;
    }

    return (
      <header className="toolbar">
        {dialogElement}
      </header>
    );
  }
}

export default ToolbarSection;
