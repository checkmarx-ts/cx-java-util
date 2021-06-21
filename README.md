# Java Utility for Checkmarx

_Built leveraging the Checkmarx Spring boot SDK found here ->
https://github.com/checkmarx-ltd/checkmarx-spring-boot-java-sdk_

The current implementation supports various functions related to
projects, roles and teams.

# Project Management

All sub-commands of the **project** comand accept the following
command line option:

The `-t` (or `--team`) command line option specifies the team to which
the project belongs.

## Set a Project’s Custom Fields

The **set-custom-fields** sub-command sets the values of the specified
project’s custom fields.

The `-s` (or `--strict`) command line option causes the
**set-custom-fields** sub-command to fail if an unrecognised custom
field is provided (by default, a message is logged and the
unrecognised field is ignored).

```
java -jar <util jar> project set-custom-fields netgoat varA=valA varB=ValB
```

## Check Whether a Full Scan Should Be Forced

The **force-full-scan** sub-command checks whether a specified amount
of time has passed since a full scan was run for the project. The exit
code indicates whether or not a full scan is required: an exit status
of 0 indicates that a full scan is required; an exit status of 1
indicates that a full scan is not required.

The `-d` (or `--duration`) command line option is used to specify the
duration. By default the measure of time is days.

The `-u` (or `--units`) command line option can be used to specify the
unit of time.

The assumption is that the **cx-java-util** program will be invoked as
part of a larger process which, if the project does not exist, will
create it. For this reason, an error is not signalled if the project
does not exist.

```
java -jar <util jar> project force-full-scan -d 7 -t /CxServer netgoat
```

# Team Management

## Create a team:

```
java -jar <util jar> team create "CxServer\SP\Checkmarx\NewTeam"
```

## Delete a team

```
java -jar <util jar> team delete "CxServer\SP\Checkmarx\NewTeam"
```

## Adding an Ldap Mapping (create team if it doesn't exist)

```
java -jar <util jar> team add-ldap -create "CxServer\SP\Checkmarx\NewTeam" "checkmarx.local" "CN=CX_USERS,DC=checkmarx,DC=local"

```

## Remove an Ldap Mapping

```
java -jar <util jar> team remove-ldap "CxServer\SP\Checkmarx\NewTeam" "checkmarx.local" "CN=CX_USERS,DC=checkmarx,DC=local"
```

# Role Management

## Map Role to LDAP DN

```
java -jar <util jar> role add-ldap "checkmarx.local" "Admin" "CN=CX_USERS,DC=checkmarx,DC=local"
```

## Remove LDAP Role Mapping:

```
java -jar <util jar> role remove-ldap "checkmarx.local" "Admin" "CN=CX_USERS,DC=checkmarx,DC=local"
```

## Checkmarx Properties 8.x:

```yaml
checkmarx:
  username: xxxx #Checkmarx service account user
  password: xxxx #Checkmarx service account password
  client-secret: 014DF517-39D1-4453-B7B3-9930C563627C
  base-url: https://cx.local #Checkmarx base url
  url: ${checkmarx.base-url}/cxrestapi
  portal-url: ${checkmarx.base-url}/cxwebinterface/Portal/CxWebService.asmx
  sdk-url: ${checkmarx.base-url}/cxwebinterface/SDK/CxSDKWebService.asmx
  portal-wsdl: ${checkmarx.base-url}/Portal/CxWebService.asmx?wsdl
  sdk-wsdl: ${checkmarx.base-url}/SDK/CxSDKWebService.asmx?wsdl
  #NOTE: Teams have Windows path pattern \My\Team
```

## Checkmarx Properties 9.0+:

```yaml
checkmarx:
  version: 9.0
  username: xxxx #Checkmarx service account user
  password: xxxx #Checkmarx service account password
  client-id: resource_owner_client
  client-secret: 014DF517-39D1-4453-B7B3-9930C563627C #Alternatively, register OIDC client
  scope: access_control_api sast_rest_api
  base-url: https://cx.local #Checkmarx base url
  url: ${checkmarx.base-url}/cxrestapi
  portal-url: ${checkmarx.base-url}/cxwebinterface/Portal/CxWebService.asmx
  sdk-url: ${checkmarx.base-url}/cxwebinterface/SDK/CxSDKWebService.asmx
  portal-wsdl: ${checkmarx.base-url}/Portal/CxWebService.asmx?wsdl
  sdk-wsdl: ${checkmarx.base-url}/SDK/CxSDKWebService.asmx?wsdl
  #NOTE: Teams have unix path pattern /My/Team
```
