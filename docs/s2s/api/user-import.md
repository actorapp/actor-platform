# User Importing

To write to someone on from one server to other you need for find User on destination server. There are some features that are strictly required to any modern messaging:

* Easy finding people by nickname, phone number or email
* Keeping user's information private
* Importing contacts and automatic contact discovery
* Notification when someone from imported contacts is registered on server

### `search_user` - Searching for a user

#### Request
`query` - string search query. Can be nickname, phone number, email or anything that can be supported in furure versions. Receiver server is responsible to strip all formating if needed.
```json
{
  "query": "steve"
}
```

#### Response
```json
{
  "results": [
    {
      "uid": 112233,
      "access_hash": "11223344556677",
      "name": "Steven Kite",
      "avatar": null,
      "username": "steve"
    }
  ]
}
