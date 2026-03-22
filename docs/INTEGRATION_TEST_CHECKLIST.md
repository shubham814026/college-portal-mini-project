# Integration Test Checklist

Mark each check after validation.

## Environment

- [ ] MySQL running
- [ ] Schema applied from db/schema.sql
- [ ] RMI server running on 1099
- [ ] Chat server running on 9100
- [ ] Chat status endpoint available on 9101
- [ ] Tomcat deployed and app accessible

## Authentication

- [ ] Valid student login redirects to dashboard
- [ ] Valid admin login redirects to dashboard
- [ ] Invalid login shows generic error
- [ ] 3 failed attempts lock account for 15 minutes
- [ ] Session timeout redirects to login?timeout=1
- [ ] Logout invalidates session

## Notice + Multicast

- [ ] Admin can post notice
- [ ] Admin notice form is reachable via /admin/notices/new
- [ ] Notice appears in student notice board
- [ ] Notice broadcast triggers status notice event
- [ ] Notice validation failure redirects with error=validation
- [ ] Notice success redirects with success=posted

## File Upload/Download

- [ ] Upload accepts pdf/docx/pptx/zip <= 10MB
- [ ] Upload rejects invalid extension
- [ ] Upload rejects >10MB
- [ ] Upload success redirects to /files?success=uploaded and shows banner
- [ ] Download existing file succeeds
- [ ] Download missing file returns 404 handling

## Alert + Multicast

- [ ] Admin can send alert
- [ ] Admin alert form is reachable via /admin/alerts/new
- [ ] Alert persists in alerts table
- [ ] Student alert banner receives message on poll
- [ ] Alert validation failure redirects with error=validation
- [ ] Alert success redirects with success=sent

## Chat + TCP

- [ ] Online users API returns users from chat status socket
- [ ] Direct message send returns sent status
- [ ] Chat offline returns chat-offline status
- [ ] Message persisted in messages table

## Logging + RMI

- [ ] LOGIN logged
- [ ] LOGOUT logged
- [ ] FILE_UPLOAD logged
- [ ] POST_NOTICE logged
- [ ] SEND_ALERT logged

## Error Paths

- [ ] RMI down shows safe service error
- [ ] Chat down does not crash web request
- [ ] DB query failure renders safe error page
- [ ] Unauthorized /admin access returns 403 handling

## Observability

- [ ] Every protected response includes X-Request-Id header
- [ ] Service and adapter failure logs include request id token
