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
        'content': {
          templateUrl: 'app/components/main/mainView.html'
        },
        'toolbar': {
          templateUrl: 'app/shared/toolbar/toolbarDefaultView.html'
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

  MainController.prototype.messages = [
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

MainController.$inject = ['$mdSidenav', '$mdMedia', '$mdBottomSheet'];

angular.module('actorWeb').controller('mainController', MainController);

//# sourceMappingURL=data:application/json;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbImFwcC5jb2ZmZWUiLCJjb25maWcuY29mZmVlIiwiY29tcG9uZW50cy9tYWluL21haW5Db250cm9sbGVyLmNvZmZlZSJdLCJuYW1lcyI6W10sIm1hcHBpbmdzIjoiQUFBQSxPQUNFLENBQUMsTUFESCxDQUNVLFVBRFYsRUFDc0IsQ0FDbEIsV0FEa0IsRUFFbEIsWUFGa0IsQ0FEdEIsQ0FBQSxDQUFBOztBQ0FBLElBQUEsTUFBQTs7QUFBQSxNQUFBLEdBQVMsU0FBQyxjQUFELEVBQWlCLGtCQUFqQixHQUFBO0FBRVAsRUFBQSxjQUNFLENBQUMsS0FESCxDQUNTLE1BRFQsRUFFSTtBQUFBLElBQUEsR0FBQSxFQUFLLEdBQUw7QUFBQSxJQUNBLEtBQUEsRUFDRTtBQUFBLE1BQUEsU0FBQSxFQUFXO0FBQUEsUUFBQSxXQUFBLEVBQWEsNENBQWI7T0FBWDtBQUFBLE1BQ0EsU0FBQSxFQUFXO0FBQUEsUUFBQSxXQUFBLEVBQWEsNENBQWI7T0FEWDtBQUFBLE1BRUEsU0FBQSxFQUFXO0FBQUEsUUFBQSxXQUFBLEVBQWEsbUNBQWI7T0FGWDtLQUZGO0dBRkosQ0FRRSxDQUFDLEtBUkgsQ0FRUyxPQVJULEVBU0k7QUFBQSxJQUFBLEdBQUEsRUFBSyxRQUFMO0FBQUEsSUFDQSxLQUFBLEVBQ0U7QUFBQSxNQUFBLFNBQUEsRUFBVztBQUFBLFFBQUEsV0FBQSxFQUFhLHFDQUFiO09BQVg7S0FGRjtHQVRKLENBQUEsQ0FBQTtTQWFBLGtCQUNFLENBQUMsU0FESCxDQUNhLEdBRGIsRUFmTztBQUFBLENBQVQsQ0FBQTs7QUFBQSxPQW1CRSxDQUFDLE1BREgsQ0FDVSxVQURWLENBRUUsQ0FBQyxNQUZILENBRVUsTUFGVixDQUdFLENBQUMsR0FISCxDQUdPLFNBQUMsVUFBRCxFQUFhLE1BQWIsRUFBcUIsWUFBckIsR0FBQTtBQUNILEVBQUEsVUFBVSxDQUFDLE1BQVgsR0FBb0IsTUFBcEIsQ0FBQTtTQUNBLFVBQVUsQ0FBQyxZQUFYLEdBQTBCLGFBRnZCO0FBQUEsQ0FIUCxDQWxCQSxDQUFBOztBQ0FBLElBQUEsY0FBQTs7QUFBQTtBQUNlLEVBQUEsd0JBQUMsVUFBRCxFQUFjLFFBQWQsRUFBeUIsY0FBekIsR0FBQTtBQUNYLElBRFksSUFBQyxDQUFBLGFBQUQsVUFDWixDQUFBO0FBQUEsSUFEeUIsSUFBQyxDQUFBLFdBQUQsUUFDekIsQ0FBQTtBQUFBLElBRG9DLElBQUMsQ0FBQSxpQkFBRCxjQUNwQyxDQUFBO0FBQUEsSUFBQSxPQUFPLENBQUMsR0FBUixDQUFZLGdCQUFaLENBQUEsQ0FEVztFQUFBLENBQWI7O0FBQUEsMkJBTUEsUUFBQSxHQUFVO0lBQ1I7QUFBQSxNQUFBLEdBQUEsRUFBSyxhQUFMO0FBQUEsTUFDQSxLQUFBLEVBQU8sc1VBRFA7S0FEUSxFQU1SO0FBQUEsTUFBQSxHQUFBLEVBQUssUUFBTDtBQUFBLE1BQ0EsS0FBQSxFQUFPLGVBRFA7S0FOUSxFQVNSO0FBQUEsTUFBQSxHQUFBLEVBQUssYUFBTDtBQUFBLE1BQ0EsS0FBQSxFQUFPLHNCQURQO0tBVFEsRUFZUjtBQUFBLE1BQUEsR0FBQSxFQUFLLGFBQUw7QUFBQSxNQUNBLEtBQUEsRUFBTyw2Q0FEUDtLQVpRO0dBTlYsQ0FBQTs7QUFBQSwyQkFzQkEsV0FBQSxHQUFhLFNBQUEsR0FBQTtBQUNYLElBQUEsT0FBTyxDQUFDLEdBQVIsQ0FBWSxhQUFaLENBQUEsQ0FBQTtXQUNBLElBQUMsQ0FBQSxVQUFELENBQVksTUFBWixDQUFtQixDQUFDLE1BQXBCLENBQUEsRUFGVztFQUFBLENBdEJiLENBQUE7O3dCQUFBOztJQURGLENBQUE7O0FBQUEsY0EyQmMsQ0FBQyxPQUFmLEdBQXlCLENBQUMsWUFBRCxFQUFlLFVBQWYsRUFBMkIsZ0JBQTNCLENBM0J6QixDQUFBOztBQUFBLE9BOEJFLENBQUMsTUFESCxDQUNVLFVBRFYsQ0FFRSxDQUFDLFVBRkgsQ0FFYyxnQkFGZCxFQUVnQyxjQUZoQyxDQTdCQSxDQUFBIiwiZmlsZSI6ImFwcC5qcyIsInNvdXJjZXNDb250ZW50IjpbImFuZ3VsYXJcbiAgLm1vZHVsZSAnYWN0b3JXZWInLCBbXG4gICAgJ3VpLnJvdXRlcidcbiAgICAnbmdNYXRlcmlhbCdcbiMgICAgJ25nU3RvcmFnZSdcbiAgXVxuIiwiY29uZmlnID0gKCRzdGF0ZVByb3ZpZGVyLCAkdXJsUm91dGVyUHJvdmlkZXIpIC0+XG5cbiAgJHN0YXRlUHJvdmlkZXJcbiAgICAuc3RhdGUgJ2hvbWUnLFxuICAgICAgdXJsOiAnLydcbiAgICAgIHZpZXdzOlxuICAgICAgICAndG9vbGJhcic6IHRlbXBsYXRlVXJsOiAnYXBwL3NoYXJlZC90b29sYmFyL3Rvb2xiYXJEZWZhdWx0Vmlldy5odG1sJ1xuICAgICAgICAnc2lkZWJhcic6IHRlbXBsYXRlVXJsOiAnYXBwL3NoYXJlZC9zaWRlYmFyL3NpZGViYXJEZWZhdWx0Vmlldy5odG1sJ1xuICAgICAgICAnY29udGVudCc6IHRlbXBsYXRlVXJsOiAnYXBwL2NvbXBvbmVudHMvbWFpbi9tYWluVmlldy5odG1sJ1xuXG4gICAgLnN0YXRlICdsb2dpbicsXG4gICAgICB1cmw6ICcvbG9naW4nXG4gICAgICB2aWV3czpcbiAgICAgICAgJ2NvbnRlbnQnOiB0ZW1wbGF0ZVVybDogJ2FwcC9jb21wb25lbnRzL2xvZ2luL2xvZ2luVmlldy5odG1sJ1xuXG4gICR1cmxSb3V0ZXJQcm92aWRlclxuICAgIC5vdGhlcndpc2UgJy8nXG5cbmFuZ3VsYXJcbiAgLm1vZHVsZSAnYWN0b3JXZWInXG4gIC5jb25maWcgY29uZmlnXG4gIC5ydW4gKCRyb290U2NvcGUsICRzdGF0ZSwgJHN0YXRlUGFyYW1zKSAtPlxuICAgICRyb290U2NvcGUuJHN0YXRlID0gJHN0YXRlXG4gICAgJHJvb3RTY29wZS4kc3RhdGVQYXJhbXMgPSAkc3RhdGVQYXJhbXNcbiIsImNsYXNzIE1haW5Db250cm9sbGVyXG4gIGNvbnN0cnVjdG9yOiAoQCRtZFNpZGVuYXYsIEAkbWRNZWRpYSwgQCRtZEJvdHRvbVNoZWV0KSAtPlxuICAgIGNvbnNvbGUubG9nICdNYWluQ29udHJvbGxlcidcbiMgICAgQCRtZEJvdHRvbVNoZWV0LnNob3dcbiMgICAgICB0ZW1wbGF0ZVVybDogJ2FwcC9zaGFyZWQvYm90dG9tU2hlZXQvYm90dG9tU2hlZXRWaWV3Lmh0bWwnXG4jICAgICAgcGFyZW50OiAnI2NvbnRlbnQnXG5cbiAgbWVzc2FnZXM6IFtcbiAgICB3aG86ICdNaW4gTGkgQ2hhbicsXG4gICAgbm90ZXM6IFwiQSByb2JvdCBtYXkgbm90IGluanVyZSBhIGh1bWFuIGJlaW5nIG9yLCB0aHJvdWdoIGluYWN0aW9uLCBhbGxvdyBhIGh1bWFuIGJlaW5nIHRvIGNvbWUgdG8gaGFybS5cbiAgICAgICAgICAgIEEgcm9ib3QgbXVzdCBvYmV5IHRoZSBvcmRlcnMgZ2l2ZW4gaXQgYnkgaHVtYW4gYmVpbmdzLCBleGNlcHQgd2hlcmUgc3VjaCBvcmRlcnMgd291bGQgY29uZmxpY3Qgd2l0aCB0aGUgRmlyc3QgTGF3LlxuICAgICAgICAgICAgQSByb2JvdCBtdXN0IHByb3RlY3QgaXRzIG93biBleGlzdGVuY2UgYXMgbG9uZyBhcyBzdWNoIHByb3RlY3Rpb24gZG9lcyBub3QgY29uZmxpY3Qgd2l0aCB0aGUgRmlyc3Qgb3IgU2Vjb25kIExhdy5cIlxuICAsXG4gICAgd2hvOiAn0KHRgtC10L/QsNC9JyxcbiAgICBub3RlczogXCLQn9GA0L7QstC10YDQutCwINCx0LvQuNC9XCJcbiAgLFxuICAgIHdobzogJ01pbiBMaSBDaGFuJyxcbiAgICBub3RlczogJ0JydW5jaCB0aGlzIHdlZWtlbmQ/JyxcbiAgLFxuICAgIHdobzogJ01pbiBMaSBDaGFuJyxcbiAgICBub3RlczogXCIgSSdsbCBiZSBpbiB5b3VyIG5laWdoYm9yaG9vZCBkb2luZyBlcnJhbmRzXCJcbiAgXVxuXG4gIG9wZW5TaWRlYmFyOiAtPlxuICAgIGNvbnNvbGUubG9nICdvcGVuU2lkZWJhcidcbiAgICBAJG1kU2lkZW5hdignbGVmdCcpLnRvZ2dsZSgpXG5cbk1haW5Db250cm9sbGVyLiRpbmplY3QgPSBbJyRtZFNpZGVuYXYnLCAnJG1kTWVkaWEnLCAnJG1kQm90dG9tU2hlZXQnXVxuXG5hbmd1bGFyXG4gIC5tb2R1bGUgJ2FjdG9yV2ViJ1xuICAuY29udHJvbGxlciAnbWFpbkNvbnRyb2xsZXInLCBNYWluQ29udHJvbGxlclxuIl0sInNvdXJjZVJvb3QiOiIvc291cmNlLyJ9