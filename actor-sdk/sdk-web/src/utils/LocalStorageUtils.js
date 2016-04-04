import debounce from 'lodash/debounce';

export function registerMaxCapacityHandler(onFull) {
  const TEST_KEY = '__actor_test__';
  const setItem = localStorage.setItem.bind(localStorage);

  function onStorageChange() {
    const n10kb = '1234567890'.repeat(10 * 1024);

    try {
      setItem(TEST_KEY, n10kb);
      localStorage.removeItem(TEST_KEY);
    } catch (e) {
      onFull(e);
    }
  }

  const changeHandler = debounce(onStorageChange, 1000);
  localStorage.setItem = (key, value) => {
    setImmediate(changeHandler);
    return setItem(key, value);
  };
}
