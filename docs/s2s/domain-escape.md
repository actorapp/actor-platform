# Domain Escaping

By default any email account can be authenticated on main actor cloud server, but in any moment domain can "escape" cloud by installing it's own server.
Escaping is performed by installing Actor server with enabled federation and setting [approriate DNS SRV records](configure-dns.md).

In an less than an hour your domain will be marked as escaped and all accounts that use this domain in cloud will be suspended.

After suspending domain, all messages for this domain will be redirected to your actor server.
