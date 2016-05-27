import fuzzaldrin from 'fuzzaldrin';
import { dispatch } from '../dispatcher/ActorAppDispatcher';
import { isPeerUser, isPeerGroup } from '../utils/PeerUtils';
import { ActionTypes } from '../constants/ActorAppConstants';
import QuickSearchStore from '../stores/QuickSearchStore';
import ComposeActionCreators from './ComposeActionCreators';
import SearchMessagesActionCreators from './SearchMessagesActionCreators';

const match = (value, query) => fuzzaldrin.score(value, query) > 0;

class SearchActionCreators {
  focus() {
    ComposeActionCreators.toggleAutoFocus(false);
    dispatch(ActionTypes.SEARCH_FOCUS);
  }

  blur() {
    ComposeActionCreators.toggleAutoFocus(true);
    dispatch(ActionTypes.SEARCH_BLUR);
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

  handleSearch(query) {
    dispatch(ActionTypes.SEARCH_SET_QUERY, { query });
    this.updateResults(query);
  }

  updateResults(query) {
    const elements = QuickSearchStore.getState();
    const results = { contacts: [], groups: [] };

    elements.filter((element) => {
      return match(element.peerInfo.title, query) ||
             match(element.peerInfo.userName, query);
    }).forEach((element) => {
      if (isPeerUser(element.peerInfo.peer)) {
        results.contacts.push(element);
      } else if (isPeerGroup(element.peerInfo.peer)) {
        results.groups.push(element);
      } else {
        console.error('Unexpected quick search element:', element);
      }
    });

    dispatch(ActionTypes.SEARCH_SET_RESULTS, { results });
  }
}

export default new SearchActionCreators();
