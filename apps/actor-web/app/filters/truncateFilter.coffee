class Truncate
  constructor: (text, length, end) ->
#    console.log '[AW]Truncate filter'
    length = 10 if isNaN(length)
    end = "…" if end == undefined

    if text.length <= length || text.length - end.length <= length
      return text
    else
      return String(text).substring(0, length - end.length) + end


angular
  .module 'actorWeb'
  .filter 'truncate', -> Truncate
