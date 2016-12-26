
## OWASP

### Cross-Site Request Forgery (CSRF)
Provide an example or link to the OWASP cheat sheet for CSRF (https://www.owasp.org/index.php/Cross-Site_Request_Forgery_(CSRF)_Prevention_Cheat_Sheet)

Discuss how the CSRF token is generated and what happens if/when it expires, or if/when it is invalid (401 Access Denied. CSRF token was not valid)
* Get the token from the response header and retry the request
* Token is alive for the duration of the session, so if the session invalidates on the server or if the user is routed to a different web server instance (via load balancer), then 

X-CSRF-TOKEN is provided on every response

X-CSRF-TOKEN is required on any "save" HTTP methods: POST, PUT, PATCH, DELETE.

/getCsrfToken web service endpoint is available to obtain a valid token when the X-CSRF-TOKEN is not available. 

Walk through some examples of how to use this when saving. 

## Role Based Access Control

### Actors (aka Users) 

### Roles
Convention: role.super, role.admin, role.doctor, role.nurse

### Permissions
Convention: api.roles.list, api.roles.get, api.roles.create, api.roles.update, api.roles.delete
api.user.list, api.user.get, api.user.create, api.user.update, api.user.delete

api.account.get, api.account.update

## OAuth2
Properties
* URL: /api/oauth/token
* Post Body: grant_type=client_credentials
* Client ID: my-client-with-secret
* Secret: secret

Example Token Generation:

```
curl -H "Accept: application/json" my-client-with-secret:secret@localhost:8080/oauth/token -d grant_type=client_credentials
```

Apply token to request header

```
Authorization: Bearer caaafafd-08bb-4b83-b9cc-a3b78e500e91
```