class MessagesController
  constructor: () ->
    console.log '[AW]MessagesController constructor'

  list: [
    who: 'Min Li Chan',
    notes: [
      'A robot may not injure a human being or, through inaction, allow a human being to come to harm.'
      'A robot must obey the orders given it by human beings, except where such orders would conflict with the First Law.'
      'A robot must protect its own existence as long as such protection does not conflict with the First or Second Law.'
    ]
  ,
    who: 'Толян',
    notes: ['Проверка блин']
  ,
    who: 'Min Li Chan',
    notes: ['Brunch this weekend?']
  ,
    who: 'Женя',
    notes: ["I'll be in your neighborhood doing errands"]
  ]

angular
  .module 'actorWeb'
  .controller 'messagesController', MessagesController
