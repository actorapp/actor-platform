var timerId;

self.addEventListener('message', function(e){
    switch (e.data.message) {
        case 'schedule':
            if (timerId) {
                clearTimeout(timerId);
                timerId = null;
            }
            timerId = setTimeout(function(){
                self.postMessage('doSchedule');
            }, e.data.delay);
            break;
        case 'cancel':
            if (timerId) {
                clearTimeout(timerId);
                timerId = null;
            }
            break;
    };
});
