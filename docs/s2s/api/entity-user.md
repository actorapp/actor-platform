# User Entity

How Users are presented in API. Users are secured with Access Hashes.

## Definition

| required | field | type | description |
|:--------:|-------|------|-------------|
|+|uid|int|User's ID|
|+|accessHash|string|Access Hash of a user|
|+|isBot|bool|Is user actually a bot|
|+|name|string|Name of the user|
|+|avatar|Avatar|Avatar of the user|
|+|contacts|Contact[]|Contact information of the user|
|-|username|string|Username of the user|
|-|about|string|About text|
|-|sex|Sex|Sex of the user|
|-|timeZone|string|Timezone of the user|
|-|preferredLangiages|string[]|Preferred languages|

## Example
```json
{
  "uid": 1122334455,
  "accessHash": "11223344556677889900",
  "isBot": false,
  "name": "Steve",
  "avatar": null,
  "username": "steve",
  "about": "Founder of Actor",
  "contacts": [],
  "sex": null,
  "timeZone": "UTC+0",
  "preferredLanguages": ["en", "us", "zh", "ar"]
}
```

## Contact Object
```json
{
  "contactType": "phone|email|web|social",
  "typeSpec": "mobile/standalone/home...",
  "value": "75551112233",
  "title": "My phone number",
  "subtitle": "Call me only on mondays"
}
```
