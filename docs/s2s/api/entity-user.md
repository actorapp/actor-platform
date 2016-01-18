# User Entity

How Users are presented in API. Users are secured with Access Hashes.

## User object
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
