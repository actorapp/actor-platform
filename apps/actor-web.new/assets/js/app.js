angular.module('actorWeb', ['ui.router', 'ngMaterial', 'ngStorage']);

var ActorConfig;

ActorConfig = (function() {
  function ActorConfig($stateProvider, $urlRouterProvider) {
    $stateProvider.state('home', {
      url: '/',
      views: {
        'sidebar': {
          templateUrl: 'app/shared/sidebar/sidebarDefaultView.html'
        },
        'toolbar': {
          templateUrl: 'app/shared/toolbar/toolbarDefaultView.html'
        },
        'content': {
          templateUrl: 'app/shared/messages/messagesListView.html'
        }
      }
    }).state('login', {
      url: '/login',
      data: {
        noLogin: true
      },
      views: {
        'content': {
          templateUrl: 'app/components/login/loginView.html'
        }
      }
    });
    $urlRouterProvider.otherwise('/');
  }

  return ActorConfig;

})();

ActorConfig.$inject = ['$stateProvider', '$urlRouterProvider'];

angular.module('actorWeb').config(ActorConfig);

var ActorRun;

ActorRun = (function() {
  function ActorRun($rootScope, $state, $stateParams, actorService) {
    console.log('[AW]Run');
    $rootScope.$state = $state;
    $rootScope.$stateParams = $stateParams;
    $rootScope.isLogedIn = null;
    $rootScope.$on('$stateChangeStart', function(event, toState, toParams, fromState, fromParams) {
      return actorService.checkAccess(event, toState, toParams, fromState, fromParams);
    });
  }

  return ActorRun;

})();

ActorRun.$inject = ['$rootScope', '$state', '$stateParams', 'actorService'];

angular.module('actorWeb').run(ActorRun);

var ActorService,
  bind = function(fn, me){ return function(){ return fn.apply(me, arguments); }; };

ActorService = (function() {
  function ActorService($rootScope, $sessionStorage) {
    this.$rootScope = $rootScope;
    this.$sessionStorage = $sessionStorage;
    this.setLoggedOut = bind(this.setLoggedOut, this);
    this.setLoggedIn = bind(this.setLoggedIn, this);
    console.log('[AW]ActorService constructor');
    this.isLoggedIn = this.$sessionStorage.isLoggedIn;
    window.jsAppLoaded = (function(_this) {
      return function() {
        _this.messenger = new actor.ActorApp;
        return _this.$rootScope.$broadcast('actor-ready');
      };
    })(this);
  }

  ActorService.prototype.setLoggedIn = function() {
    console.log('[AW]ActorService setLoggedIn()');
    this.isLoggedIn = true;
    this.$rootScope.isLogedIn = true;
    this.$sessionStorage.isLogedIn = true;
    return this.$rootScope.$state.go('home');
  };

  ActorService.prototype.setLoggedOut = function() {
    console.log('[AW]ActorService setLoggedOut()');
    this.isLoggedIn = false;
    this.$rootScope.isLogedIn = false;
    this.$sessionStorage.isLogedIn = false;
    return this.$rootScope.$state.go('login');
  };

  ActorService.prototype.requestSms = function(phone) {
    console.log('[AW]ActorService requestSms()');
    return this.messenger.requestSms(phone.toString(), function(state) {
      return console.log('[AW]ActorService requestSms(): state:', state);
    }, function(tag, message, canTryAgain, state) {
      console.log('[AW]ActorService requestSms(): error');
      console.log('[AW]ActorService requestSms(): tag:', tag);
      console.log('[AW]ActorService requestSms(): message:', message);
      console.log('[AW]ActorService requestSms(): canTryAgain:', canTryAgain);
      return console.log('[AW]ActorService requestSms(): state:', state);
    });
  };

  ActorService.prototype.sendCode = function(code) {
    var setLoggedIn;
    console.log('[AW]ActorService sendCode()');
    setLoggedIn = this.setLoggedIn;
    return this.messenger.sendCode(code, function(state) {
      console.log('[AW]ActorService sendCode(): state:', state);
      if (state === 'logged_in') {
        return setLoggedIn();
      }
    }, function(tag, message, canTryAgain, state) {
      console.log('[AW]ActorService sendCode(): error');
      console.log('[AW]ActorService sendCode(): tag:', tag);
      console.log('[AW]ActorService sendCode(): message:', message);
      console.log('[AW]ActorService sendCode(): canTryAgain:', canTryAgain);
      return console.log('[AW]ActorService sendCode(): state:', state);
    });
  };

  ActorService.prototype.getDialogs = function(callback) {
    console.log('[AW]ActorService getDialogs()');
    return this.messenger.bindDialogs(function(items) {
      return callback(items);
    });
  };

  ActorService.prototype.checkAccess = function(event, toState, toParams, fromState, fromParams) {
    if (toState.data !== void 0) {
      if (toState.data.noLogin !== void 0 && toState.data.noLogin) {
        console.log('[AW]ActorService checkAccess(): before login');
      }
    } else {
      if (this.$sessionStorage.isLogedIn) {
        console.log('[AW]ActorService checkAccess(): authenticated');
        return this.$rootScope.isLogedIn = this.$sessionStorage.isLogedIn;
      } else {
        console.log('[AW]ActorService checkAccess(): redirect to login');
        event.preventDefault();
        return this.$rootScope.$state.go('login');
      }
    }
  };

  return ActorService;

})();

ActorService.$inject = ['$rootScope', '$sessionStorage'];

angular.module('actorWeb').service('actorService', ActorService);

var LoginController;

LoginController = (function() {
  function LoginController(actorService) {
    this.actorService = actorService;
    console.log('[AW]LoginController constructor');
  }

  LoginController.prototype.isCodeRequested = false;

  LoginController.prototype.requestCode = function(phone) {
    console.log('[AW]LoginController requestCode()');
    this.actorService.requestSms(phone);
    return this.isCodeRequested = true;
  };

  LoginController.prototype.checkCode = function(code) {
    console.log('[AW]LoginController checkCode()');
    return this.actorService.sendCode(code);
  };

  return LoginController;

})();

LoginController.$inject = ['actorService'];

angular.module('actorWeb').controller('loginController', LoginController);

var MainController;

MainController = (function() {
  function MainController($mdSidenav, $mdMedia, $mdBottomSheet, actorService) {
    this.$mdSidenav = $mdSidenav;
    this.$mdMedia = $mdMedia;
    this.$mdBottomSheet = $mdBottomSheet;
    this.actorService = actorService;
    console.log('[AW]MainController constructor');
  }

  MainController.prototype.showBottomSheet = function() {
    console.log('[AW]MainController showBottomSheet()');
    return this.$mdBottomSheet.show({
      templateUrl: 'app/shared/bottomSheet/bottomSheetView.html',
      parent: '#content',
      disableParentScroll: false
    });
  };

  MainController.prototype.openSidebar = function() {
    console.log('[AW]MainController openSidebar()');
    return this.$mdSidenav('left').toggle();
  };

  return MainController;

})();

MainController.$inject = ['$mdSidenav', '$mdMedia', '$mdBottomSheet', 'actorService'];

angular.module('actorWeb').controller('mainController', MainController);

var ChatsController;

ChatsController = (function() {
  function ChatsController($scope, actorService) {
    this.$scope = $scope;
    this.actorService = actorService;
    console.log('[AW]ChatsController constructor');
    this.$scope.$on('actor-ready', (function(_this) {
      return function() {
        return _this.getChats();
      };
    })(this));
  }

  ChatsController.prototype.getChats = function() {
    console.log('[AW]ChatsController getChats()');
    this.actorService.getDialogs((function(_this) {
      return function(items) {
        items.forEach(function(item) {
          return console.log(item);
        });
        return _this.list = items;
      };
    })(this));
    return console.log('[AW]ChatsController @list:', this.list);
  };

  return ChatsController;

})();

ChatsController.$inject = ['$scope', 'actorService'];

angular.module('actorWeb').controller('chatsController', ChatsController);

var MessagesController;

MessagesController = (function() {
  function MessagesController() {
    console.log('[AW]MessagesController constructor');
  }

  MessagesController.prototype.list = [
    {
      who: 'Min Li Chan',
      notes: ['A robot may not injure a human being or, through inaction, allow a human being to come to harm.', 'A robot must obey the orders given it by human beings, except where such orders would conflict with the First Law.', 'A robot must protect its own existence as long as such protection does not conflict with the First or Second Law.']
    }, {
      who: 'Толян',
      notes: ['Проверка блин']
    }, {
      who: 'Min Li Chan',
      notes: ['Brunch this weekend?']
    }, {
      who: 'Женя',
      notes: ["I'll be in your neighborhood doing errands"]
    }
  ];

  return MessagesController;

})();

angular.module('actorWeb').controller('messagesController', MessagesController);



//# sourceMappingURL=data:application/json;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbImFwcC5jb2ZmZWUiLCJhcHAuY29uZmlnLmNvZmZlZSIsImFwcC5ydW4uY29mZmVlIiwic2VydmljZXMvYWN0b3JTZXJ2aWNlLmNvZmZlZSIsImNvbXBvbmVudHMvbG9naW4vbG9naW5Db250cm9sbGVyLmNvZmZlZSIsImNvbXBvbmVudHMvbWFpbi9tYWluQ29udHJvbGxlci5jb2ZmZWUiLCJzaGFyZWQvY2hhdHMvY2hhdHNDb250cm9sbGVyLmNvZmZlZSIsInNoYXJlZC9tZXNzYWdlcy9tZXNzYWdlc0NvbnRyb2xsZXIuY29mZmVlIiwic2hhcmVkL3NpZGViYXIvc2lkZWJhckNvbnRyb2xsZXIuY29mZmVlIl0sIm5hbWVzIjpbXSwibWFwcGluZ3MiOiJBQUFBLE9BQ0UsQ0FBQyxNQURILENBQ1UsVUFEVixFQUNzQixDQUNsQixXQURrQixFQUVsQixZQUZrQixFQUdsQixXQUhrQixDQUR0QixDQUFBLENBQUE7O0FDQUEsSUFBQSxXQUFBOztBQUFBO0FBQ2UsRUFBQSxxQkFBQyxjQUFELEVBQWlCLGtCQUFqQixHQUFBO0FBQ1gsSUFBQSxjQUNFLENBQUMsS0FESCxDQUNTLE1BRFQsRUFFSTtBQUFBLE1BQUEsR0FBQSxFQUFLLEdBQUw7QUFBQSxNQUNBLEtBQUEsRUFDRTtBQUFBLFFBQUEsU0FBQSxFQUFXO0FBQUEsVUFBQSxXQUFBLEVBQWEsNENBQWI7U0FBWDtBQUFBLFFBQ0EsU0FBQSxFQUFXO0FBQUEsVUFBQSxXQUFBLEVBQWEsNENBQWI7U0FEWDtBQUFBLFFBRUEsU0FBQSxFQUFXO0FBQUEsVUFBQSxXQUFBLEVBQWEsMkNBQWI7U0FGWDtPQUZGO0tBRkosQ0FRRSxDQUFDLEtBUkgsQ0FRUyxPQVJULEVBU0k7QUFBQSxNQUFBLEdBQUEsRUFBSyxRQUFMO0FBQUEsTUFDQSxJQUFBLEVBQ0U7QUFBQSxRQUFBLE9BQUEsRUFBUyxJQUFUO09BRkY7QUFBQSxNQUdBLEtBQUEsRUFDRTtBQUFBLFFBQUEsU0FBQSxFQUFXO0FBQUEsVUFBQSxXQUFBLEVBQWEscUNB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ZWInLCBbXG4gICAgJ3VpLnJvdXRlcidcbiAgICAnbmdNYXRlcmlhbCdcbiAgICAnbmdTdG9yYWdlJ1xuICBdXG4iLCJjbGFzcyBBY3RvckNvbmZpZ1xuICBjb25zdHJ1Y3RvcjogKCRzdGF0ZVByb3ZpZGVyLCAkdXJsUm91dGVyUHJvdmlkZXIpIC0+XG4gICAgJHN0YXRlUHJvdmlkZXJcbiAgICAgIC5zdGF0ZSAnaG9tZScsXG4gICAgICAgIHVybDogJy8nXG4gICAgICAgIHZpZXdzOlxuICAgICAgICAgICdzaWRlYmFyJzogdGVtcGxhdGVVcmw6ICdhcHAvc2hhcmVkL3NpZGViYXIvc2lkZWJhckRlZmF1bHRWaWV3Lmh0bWwnXG4gICAgICAgICAgJ3Rvb2xiYXInOiB0ZW1wbGF0ZVVybDogJ2FwcC9zaGFyZWQvdG9vbGJhci90b29sYmFyRGVmYXVsdFZpZXcuaHRtbCdcbiAgICAgICAgICAnY29udGVudCc6IHRlbXBsYXRlVXJsOiAnYXBwL3NoYXJlZC9tZXNzYWdlcy9tZXNzYWdlc0xpc3RWaWV3Lmh0bWwnXG5cbiAgICAgIC5zdGF0ZSAnbG9naW4nLFxuICAgICAgICB1cmw6ICcvbG9naW4nXG4gICAgICAgIGRhdGE6XG4gICAgICAgICAgbm9Mb2dpbjogdHJ1ZVxuICAgICAgICB2aWV3czpcbiAgICAgICAgICAnY29udGVudCc6IHRlbXBsYXRlVXJsOiAnYXBwL2NvbXBvbmVudHMvbG9naW4vbG9naW5WaWV3Lmh0bWwnXG5cbiAgICAkdXJsUm91dGVyUHJvdmlkZXJcbiAgICAgIC5vdGhlcndpc2UgJy8nXG5cbkFjdG9yQ29uZmlnLiRpbmplY3QgPSBbJyRzdGF0ZVByb3ZpZGVyJywgJyR1cmxSb3V0ZXJQcm92aWRlciddXG5cbmFuZ3VsYXJcbiAgLm1vZHVsZSAnYWN0b3JXZWInXG4gIC5jb25maWcgQWN0b3JDb25maWdcbiIsImNsYXNzIEFjdG9yUnVuXG4gIGNvbnN0cnVjdG9yOiAoJHJvb3RTY29wZSwgJHN0YXRlLCAkc3RhdGVQYXJhbXMsIGFjdG9yU2VydmljZSkgLT5cbiAgICBjb25zb2xlLmxvZyAnW0FXXVJ1bidcbiAgICAkcm9vdFNjb3BlLiRzdGF0ZSA9ICRzdGF0ZVxuICAgICRyb290U2NvcGUuJHN0YXRlUGFyYW1zID0gJHN0YXRlUGFyYW1zXG5cbiAgICAkcm9vdFNjb3BlLmlzTG9nZWRJbiA9IG51bGxcbiAgICAkcm9vdFNjb3BlLiRvbiAnJHN0YXRlQ2hhbmdlU3RhcnQnLCAoZXZlbnQsIHRvU3RhdGUsIHRvUGFyYW1zLCBmcm9tU3RhdGUsIGZyb21QYXJhbXMpIC0+XG4gICAgICBhY3RvclNlcnZpY2UuY2hlY2tBY2Nlc3MoZXZlbnQsIHRvU3RhdGUsIHRvUGFyYW1zLCBmcm9tU3RhdGUsIGZyb21QYXJhbXMpXG5cbkFjdG9yUnVuLiRpbmplY3QgPSBbJyRyb290U2NvcGUnLCAnJHN0YXRlJywgJyRzdGF0ZVBhcmFtcycsICdhY3RvclNlcnZpY2UnXVxuXG5hbmd1bGFyXG4gIC5tb2R1bGUgJ2FjdG9yV2ViJ1xuICAucnVuIEFjdG9yUnVuXG4iLCJjbGFzcyBBY3RvclNlcnZpY2VcbiAgY29uc3RydWN0b3I6IChAJHJvb3RTY29wZSwgQCRzZXNzaW9uU3RvcmFnZSkgLT5cbiAgICBjb25zb2xlLmxvZyAnW0FXXUFjdG9yU2VydmljZSBjb25zdHJ1Y3RvcidcbiAgICBAaXNMb2dnZWRJbiA9IEAkc2Vzc2lvblN0b3JhZ2UuaXNMb2dnZWRJblxuICAgIHdpbmRvdy5qc0FwcExvYWRlZCA9ID0+XG4gICAgICBAbWVzc2VuZ2VyID0gbmV3IGFjdG9yLkFjdG9yQXBwXG4gICAgICBAJHJvb3RTY29wZS4kYnJvYWRjYXN0ICdhY3Rvci1yZWFkeSdcblxuICBzZXRMb2dnZWRJbjogKCkgPT5cbiAgICBjb25zb2xlLmxvZyAnW0FXXUFjdG9yU2VydmljZSBzZXRMb2dnZWRJbigpJ1xuICAgIEBpc0xvZ2dlZEluID0gdHJ1ZVxuICAgIEAkcm9vdFNjb3BlLmlzTG9nZWRJbiA9IHRydWVcbiAgICBAJHNlc3Npb25TdG9yYWdlLmlzTG9nZWRJbiA9IHRydWVcbiAgICBAJHJvb3RTY29wZS4kc3RhdGUuZ28oJ2hvbWUnKVxuXG4gIHNldExvZ2dlZE91dDogKCkgPT5cbiAgICBjb25zb2xlLmxvZyAnW0FXXUFjdG9yU2VydmljZSBzZXRMb2dnZWRPdXQoKSdcbiAgICBAaXNMb2dnZWRJbiA9IGZhbHNlXG4gICAgQCRyb290U2NvcGUuaXNMb2dlZEluID0gZmFsc2VcbiAgICBAJHNlc3Npb25TdG9yYWdlLmlzTG9nZWRJbiA9IGZhbHNlXG4gICAgQCRyb290U2NvcGUuJHN0YXRlLmdvKCdsb2dpbicpXG5cbiAgcmVxdWVzdFNtczogKHBob25lKSAtPlxuICAgIGNvbnNvbGUubG9nICdbQVddQWN0b3JTZXJ2aWNlIHJlcXVlc3RTbXMoKSdcbiAgICBAbWVzc2VuZ2VyLnJlcXVlc3RTbXMgcGhvbmUudG9TdHJpbmcoKSwgKHN0YXRlKSAtPlxuICAgICAgY29uc29sZS5sb2cgJ1tBV11BY3RvclNlcnZpY2UgcmVxdWVzdFNtcygpOiBzdGF0ZTonLCBzdGF0ZVxuICAgICwgKHRhZywgbWVzc2FnZSwgY2FuVHJ5QWdhaW4sIHN0YXRlKSAtPlxuICAgICAgY29uc29sZS5sb2cgJ1tBV11BY3RvclNlcnZpY2UgcmVxdWVzdFNtcygpOiBlcnJvcidcbiAgICAgIGNvbnNvbGUubG9nICdbQVddQWN0b3JTZXJ2aWNlIHJlcXVlc3RTbXMoKTogdGFnOicsIHRhZ1xuICAgICAgY29uc29sZS5sb2cgJ1tBV11BY3RvclNlcnZpY2UgcmVxdWVzdFNtcygpOiBtZXNzYWdlOicsIG1lc3NhZ2VcbiAgICAgIGNvbnNvbGUubG9nICdbQVddQWN0b3JTZXJ2aWNlIHJlcXVlc3RTbXMoKTogY2FuVHJ5QWdhaW46JywgY2FuVHJ5QWdhaW5cbiAgICAgIGNvbnNvbGUubG9nICdbQVddQWN0b3JTZXJ2aWNlIHJlcXVlc3RTbXMoKTogc3RhdGU6Jywgc3RhdGVcblxuICBzZW5kQ29kZTogKGNvZGUpIC0+XG4gICAgY29uc29sZS5sb2cgJ1tBV11BY3RvclNlcnZpY2Ugc2VuZENvZGUoKSdcbiAgICBzZXRMb2dnZWRJbiA9IEBzZXRMb2dnZWRJblxuICAgIEBtZXNzZW5nZXIuc2VuZENvZGUgY29kZSwgKHN0YXRlKSAtPlxuICAgICAgY29uc29sZS5sb2cgJ1tBV11BY3RvclNlcnZpY2Ugc2VuZENvZGUoKTogc3RhdGU6Jywgc3RhdGVcbiAgICAgIHNldExvZ2dlZEluKCkgaWYgc3RhdGUgPT0gJ2xvZ2dlZF9pbidcbiAgICAsICh0YWcsIG1lc3NhZ2UsIGNhblRyeUFnYWluLCBzdGF0ZSkgLT5cbiAgICAgIGNvbnNvbGUubG9nICdbQVddQWN0b3JTZXJ2aWNlIHNlbmRDb2RlKCk6IGVycm9yJ1xuICAgICAgY29uc29sZS5sb2cgJ1tBV11BY3RvclNlcnZpY2Ugc2VuZENvZGUoKTogdGFnOicsIHRhZ1xuICAgICAgY29uc29sZS5sb2cgJ1tBV11BY3RvclNlcnZpY2Ugc2VuZENvZGUoKTogbWVzc2FnZTonLCBtZXNzYWdlXG4gICAgICBjb25zb2xlLmxvZyAnW0FXXUFjdG9yU2VydmljZSBzZW5kQ29kZSgpOiBjYW5UcnlBZ2FpbjonLCBjYW5UcnlBZ2FpblxuICAgICAgY29uc29sZS5sb2cgJ1tBV11BY3RvclNlcnZpY2Ugc2VuZENvZGUoKTogc3RhdGU6Jywgc3RhdGVcblxuICBnZXREaWFsb2dzOiAoY2FsbGJhY2spIC0+XG4gICAgY29uc29sZS5sb2cgJ1tBV11BY3RvclNlcnZpY2UgZ2V0RGlhbG9ncygpJ1xuICAgIEBtZXNzZW5nZXIuYmluZERpYWxvZ3MgKGl0ZW1zKSAtPiBjYWxsYmFjayBpdGVtc1xuXG4gIGNoZWNrQWNjZXNzOiAoZXZlbnQsIHRvU3RhdGUsIHRvUGFyYW1zLCBmcm9tU3RhdGUsIGZyb21QYXJhbXMpIC0+XG4gICAgaWYgdG9TdGF0ZS5kYXRhICE9IHVuZGVmaW5lZFxuICAgICAgaWYgdG9TdGF0ZS5kYXRhLm5vTG9naW4gIT0gdW5kZWZpbmVkICYmIHRvU3RhdGUuZGF0YS5ub0xvZ2luXG4gICAgICAgIGNvbnNvbGUubG9nICdbQVddQWN0b3JTZXJ2aWNlIGNoZWNrQWNjZXNzKCk6IGJlZm9yZSBsb2dpbidcbiAgICAgICAgcmV0dXJuXG4gICAgZWxzZVxuICAgICAgaWYgQCRzZXNzaW9uU3RvcmFnZS5pc0xvZ2VkSW5cbiAgICAgICAgY29uc29sZS5sb2cgJ1tBV11BY3RvclNlcnZpY2UgY2hlY2tBY2Nlc3MoKTogYXV0aGVudGljYXRlZCdcbiAgICAgICAgQCRyb290U2NvcGUuaXNMb2dlZEluID0gQCRzZXNzaW9uU3RvcmFnZS5pc0xvZ2VkSW5cbiAgICAgIGVsc2VcbiAgICAgICAgY29uc29sZS5sb2cgJ1tBV11BY3RvclNlcnZpY2UgY2hlY2tBY2Nlc3MoKTogcmVkaXJlY3QgdG8gbG9naW4nXG4gICAgICAgIGV2ZW50LnByZXZlbnREZWZhdWx0KClcbiAgICAgICAgQCRyb290U2NvcGUuJHN0YXRlLmdvKCdsb2dpbicpXG5cbkFjdG9yU2VydmljZS4kaW5qZWN0ID0gWyckcm9vdFNjb3BlJywgJyRzZXNzaW9uU3RvcmFnZSddXG5cbmFuZ3VsYXJcbiAgLm1vZHVsZSAnYWN0b3JXZWInXG4gIC5zZXJ2aWNlICdhY3RvclNlcnZpY2UnLCBBY3RvclNlcnZpY2VcbiIsImNsYXNzIExvZ2luQ29udHJvbGxlclxuICBjb25zdHJ1Y3RvcjogKEBhY3RvclNlcnZpY2UpIC0+XG4gICAgY29uc29sZS5sb2cgJ1tBV11Mb2dpbkNvbnRyb2xsZXIgY29uc3RydWN0b3InXG5cbiAgaXNDb2RlUmVxdWVzdGVkOiBmYWxzZVxuXG4gIHJlcXVlc3RDb2RlOiAocGhvbmUpIC0+XG4gICAgY29uc29sZS5sb2cgJ1tBV11Mb2dpbkNvbnRyb2xsZXIgcmVxdWVzdENvZGUoKSdcbiAgICBAYWN0b3JTZXJ2aWNlLnJlcXVlc3RTbXMgcGhvbmVcbiAgICBAaXNDb2RlUmVxdWVzdGVkID0gdHJ1ZVxuXG4gIGNoZWNrQ29kZTogKGNvZGUpIC0+XG4gICAgY29uc29sZS5sb2cgJ1tBV11Mb2dpbkNvbnRyb2xsZXIgY2hlY2tDb2RlKCknXG4gICAgQGFjdG9yU2VydmljZS5zZW5kQ29kZSBjb2RlXG5cbkxvZ2luQ29udHJvbGxlci4kaW5qZWN0ID0gWydhY3RvclNlcnZpY2UnXVxuXG5hbmd1bGFyXG4gIC5tb2R1bGUgJ2FjdG9yV2ViJ1xuICAuY29udHJvbGxlciAnbG9naW5Db250cm9sbGVyJywgTG9naW5Db250cm9sbGVyXG4iLCJjbGFzcyBNYWluQ29udHJvbGxlclxuICBjb25zdHJ1Y3RvcjogKEAkbWRTaWRlbmF2LCBAJG1kTWVkaWEsIEAkbWRCb3R0b21TaGVldCwgQGFjdG9yU2VydmljZSkgLT5cbiAgICBjb25zb2xlLmxvZyAnW0FXXU1haW5Db250cm9sbGVyIGNvbnN0cnVjdG9yJ1xuXG4gIHNob3dCb3R0b21TaGVldDogLT5cbiAgICBjb25zb2xlLmxvZyAnW0FXXU1haW5Db250cm9sbGVyIHNob3dCb3R0b21TaGVldCgpJ1xuICAgIEAkbWRCb3R0b21TaGVldC5zaG93XG4gICAgICB0ZW1wbGF0ZVVybDogJ2FwcC9zaGFyZWQvYm90dG9tU2hlZXQvYm90dG9tU2hlZXRWaWV3Lmh0bWwnXG4gICAgICBwYXJlbnQ6ICcjY29udGVudCdcbiAgICAgIGRpc2FibGVQYXJlbnRTY3JvbGw6IGZhbHNlXG5cbiAgb3BlblNpZGViYXI6IC0+XG4gICAgY29uc29sZS5sb2cgJ1tBV11NYWluQ29udHJvbGxlciBvcGVuU2lkZWJhcigpJ1xuICAgIEAkbWRTaWRlbmF2KCdsZWZ0JykudG9nZ2xlKClcblxuTWFpbkNvbnRyb2xsZXIuJGluamVjdCA9IFsnJG1kU2lkZW5hdicsICckbWRNZWRpYScsICckbWRCb3R0b21TaGVldCcsICdhY3RvclNlcnZpY2UnXVxuXG5hbmd1bGFyXG4gIC5tb2R1bGUgJ2FjdG9yV2ViJ1xuICAuY29udHJvbGxlciAnbWFpbkNvbnRyb2xsZXInLCBNYWluQ29udHJvbGxlclxuIiwiY2xhc3MgQ2hhdHNDb250cm9sbGVyXG4gIGNvbnN0cnVjdG9yOiAoQCRzY29wZSwgQGFjdG9yU2VydmljZSkgLT5cbiAgICBjb25zb2xlLmxvZyAnW0FXXUNoYXRzQ29udHJvbGxlciBjb25zdHJ1Y3RvcidcbiAgICBAJHNjb3BlLiRvbiAnYWN0b3ItcmVhZHknLCA9PiBAZ2V0Q2hhdHMoKVxuXG4gIGdldENoYXRzOiAtPlxuICAgIGNvbnNvbGUubG9nICdbQVddQ2hhdHNDb250cm9sbGVyIGdldENoYXRzKCknXG4gICAgQGFjdG9yU2VydmljZS5nZXREaWFsb2dzIChpdGVtcykgPT5cbiAgICAgIGl0ZW1zLmZvckVhY2ggKGl0ZW0pIC0+XG4gICAgICAgIGNvbnNvbGUubG9nIGl0ZW1cblxuICAgICAgQGxpc3QgPSBpdGVtc1xuICAgIGNvbnNvbGUubG9nICdbQVddQ2hhdHNDb250cm9sbGVyIEBsaXN0OicsIEBsaXN0XG5cbkNoYXRzQ29udHJvbGxlci4kaW5qZWN0ID0gWyckc2NvcGUnLCAnYWN0b3JTZXJ2aWNlJ11cblxuYW5ndWxhclxuICAubW9kdWxlICdhY3RvcldlYidcbiAgLmNvbnRyb2xsZXIgJ2NoYXRzQ29udHJvbGxlcicsIENoYXRzQ29udHJvbGxlclxuIiwiY2xhc3MgTWVzc2FnZXNDb250cm9sbGVyXG4gIGNvbnN0cnVjdG9yOiAoKSAtPlxuICAgIGNvbnNvbGUubG9nICdbQVddTWVzc2FnZXNDb250cm9sbGVyIGNvbnN0cnVjdG9yJ1xuXG4gIGxpc3Q6IFtcbiAgICB3aG86ICdNaW4gTGkgQ2hhbicsXG4gICAgbm90ZXM6IFtcbiAgICAgICdBIHJvYm90IG1heSBub3QgaW5qdXJlIGEgaHVtYW4gYmVpbmcgb3IsIHRocm91Z2ggaW5hY3Rpb24sIGFsbG93IGEgaHVtYW4gYmVpbmcgdG8gY29tZSB0byBoYXJtLidcbiAgICAgICdBIHJvYm90IG11c3Qgb2JleSB0aGUgb3JkZXJzIGdpdmVuIGl0IGJ5IGh1bWFuIGJlaW5ncywgZXhjZXB0IHdoZXJlIHN1Y2ggb3JkZXJzIHdvdWxkIGNvbmZsaWN0IHdpdGggdGhlIEZpcnN0IExhdy4nXG4gICAgICAnQSByb2JvdCBtdXN0IHByb3RlY3QgaXRzIG93biBleGlzdGVuY2UgYXMgbG9uZyBhcyBzdWNoIHByb3RlY3Rpb24gZG9lcyBub3QgY29uZmxpY3Qgd2l0aCB0aGUgRmlyc3Qgb3IgU2Vjb25kIExhdy4nXG4gICAgXVxuICAsXG4gICAgd2hvOiAn0KLQvtC70Y/QvScsXG4gICAgbm90ZXM6IFsn0J/RgNC+0LLQtdGA0LrQsCDQsdC70LjQvSddXG4gICxcbiAgICB3aG86ICdNaW4gTGkgQ2hhbicsXG4gICAgbm90ZXM6IFsnQnJ1bmNoIHRoaXMgd2Vla2VuZD8nXVxuICAsXG4gICAgd2hvOiAn0JbQtdC90Y8nLFxuICAgIG5vdGVzOiBbXCJJJ2xsIGJlIGluIHlvdXIgbmVpZ2hib3Job29kIGRvaW5nIGVycmFuZHNcIl1cbiAgXVxuXG5hbmd1bGFyXG4gIC5tb2R1bGUgJ2FjdG9yV2ViJ1xuICAuY29udHJvbGxlciAnbWVzc2FnZXNDb250cm9sbGVyJywgTWVzc2FnZXNDb250cm9sbGVyXG4iLCIjIGNsYXNzIFNpZGViYXJDb250cm9sbGVyXG4jICAgY29uc3RydWN0b3I6IC0+XG4jICAgICBjb25zb2xlLmxvZyAnW0FXXVNpZGViYXJDb250cm9sbGVyIGNvbnN0cnVjdG9yJ1xuI1xuIyBhbmd1bGFyXG4jICAgLm1vZHVsZSAnYWN0b3JXZWInXG4jICAgLmNvbnRyb2xsZXIgJ3NpZGViYXJDb250cm9sbGVyJywgU2lkZWJhckNvbnRyb2xsZXJcbiJdLCJzb3VyY2VSb290IjoiL3NvdXJjZS8ifQ==