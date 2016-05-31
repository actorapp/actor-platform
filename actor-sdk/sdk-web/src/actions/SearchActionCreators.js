import { dispatch } from '../dispatcher/ActorAppDispatcher';
import history from '../utils/history';
import { search } from '../utils/SearchUtils';
import { ActionTypes } from '../constants/ActorAppConstants';
import QuickSearchStore from '../stores/QuickSearchStore';
import ComposeActionCreators from './ComposeActionCreators';
import SearchMessagesActionCreators from './SearchMessagesActionCreators';

class SearchActionCreators {
  focus() {
    ComposeActionCreators.toggleAutoFocus(false);
    dispatch(ActionTypes.SEARCH_FOCUS);
  }

  blur() {
    dispatch(ActionTypes.SEARCH_BLUR);
    ComposeActionCreators.toggleAutoFocus(true);
  }

  clear() {
    dispatch(ActionTypes.SEARCH_CLEAR);
    ComposeActionCreators.toggleAutoFocus(true);
  }

  goToMessagesSearch(query) {
    SearchMessagesActionCreators.open();
    SearchMessagesActionCreators.setQuery(query);
    dispatch(ActionTypes.SEARCH_CLEAR);
  }

  goToContact(contact) {
    dispatch(ActionTypes.SEARCH_CLEAR);
    history.push(`/im/${contact.peerInfo.peer.key}`);
  }

  handleSearch(query) {
    dispatch(ActionTypes.SEARCH_SET_QUERY, { query });
    this.updateResults(query);
  }

  updateResults(query) {
    const elements = QuickSearchStore.getState();
    const results = search(query, elements, (element) => {
      return [
        element.peerInfo.title,
        element.peerInfo.userName
      ];
    });

    dispatch(ActionTypes.SEARCH_SET_RESULTS, { results });
  }
}

export default new SearchActionCreators();
