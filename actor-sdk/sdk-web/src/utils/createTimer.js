const padTime = (num) => num < 10 ? '0' + num : num;

function createTimer(onUpdate) {
  let min = 0;
  let sec = 0;
  const id = setInterval(() => {
    if (sec === 59) {
      sec = 0;
      min++;
    } else {
      sec++;
    }

    onUpdate(padTime(min) + ':' + padTime(sec));
  }, 1000);

  return {
    stop() {
      clearInterval(id);
    }
  };
}

export default createTimer;
