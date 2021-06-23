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

The first non-option command line argument is the name of the
Checkmarx project. All subsequent non-option arguments are expected to
be of the form `name=value` where `name` is the name of the custom
field and `value` is the new value to be assigned to the field.

### Example
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

The only non-option argument is the name of the project.

The assumption is that the **cx-java-util** program will be invoked as
part of a larger process which, if the project does not exist, will
create it. For this reason, an error is not signalled if the project
does not exist.

### Example
```
java -jar <util jar> project force-full-scan -d 7 -t /CxServer netgoat
```

# Role Management

## Map Role to an LDAP DN

The **add-ldap** subcommand maps a role to an LDAP distinguished name (dn).

### Example
```
java -jar <util jar> role add-ldap "checkmarx.local" "Admin" "CN=CX_USERS,DC=checkmarx,DC=local"
```

## Remove an LDAP Role Mapping:

The **remove-ldap** subcommand removes a mapping of a role to an LDAP distinguished name.

### Example
```
java -jar <util jar> role remove-ldap "checkmarx.local" "Admin" "CN=CX_USERS,DC=checkmarx,DC=local"
```

# Team Management

## Create a Team

The **create** subcommand creates a new team. The only argument is the
name of the team to be created.

### Example
```
java -jar <util jar> team create "CxServer\SP\Checkmarx\NewTeam"
```

## Delete a Team

The **delete** subcommand deletes a team. The only argument is the
fully qualified name of the team to be deleted.

### Example
```
java -jar <util jar> team delete "CxServer\SP\Checkmarx\NewTeam"
```

## Add an LDAP Mapping

The **add-ldap** subcommand maps an LDAP distinguished name to a
Checkmarx team, creating the team if it does not already exist.

### Example
```
java -jar <util jar> team add-ldap -create "CxServer\SP\Checkmarx\NewTeam" "checkmarx.local" "CN=CX_USERS,DC=checkmarx,DC=local"

```

## Remove an LDAP Mapping

The **remove-ldap** subcommand removes a mapping of an LDAP
distinguished name to a Checkmarx team.

### Example
```
java -jar <util jar> team remove-ldap "CxServer\SP\Checkmarx\NewTeam" "checkmarx.local" "CN=CX_USERS,DC=checkmarx,DC=local"
```

# Configuration

The **cx-java-util** program can be configured using command line
options, environment variables or a configuration file (or a
combination of these). See the [Checkmarx Spring Boot Java SDK](https://github.com/checkmarx-ltd/checkmarx-spring-boot-java-sdk)
and the [Spring Boot documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)
for more details.

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
