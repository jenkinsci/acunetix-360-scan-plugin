Jenkins plugin for Acunetix 360

## Description

Allows users to start security scans via Acunetix 360 and see their
reports in Jenkins 

## Features

### Global Settings

 Acunetix 360 plugin needs the admin user to define the API settings
only once.

![](https://wiki.jenkins.io/download/attachments/175211006/image2019-7-4_10-37-16.png?version=1&modificationDate=1562225835000&api=v2){height="150"}

### Global Settings Override

Global settings can be overridden in pipeline scripts by
giving ncApiToken and/or ncServerURL parameters.

#### Example Script

step(\[$class: 'ACXScanBuilder', ncScanType: '', ncWebsiteId:
'64de5546-c58d-4352-574e-aa3a02c258b4', ncProfileId:
'6b0a49f9-2603-4f81-5802-aa7f0260f280'\])

![](https://wiki.jenkins.io/download/attachments/175211006/JenkinsSS.PNG?version=2&modificationDate=1569842645000&api=v2)

### Scan Task

 Once you define global API settings, the plugin retrieves available
scan settings such as scannable website list and scan profile names. You
can easily select relevant settings.

![](https://wiki.jenkins.io/download/attachments/175211006/jenkinsbuild.PNG?version=1&modificationDate=1569265085000&api=v2){width="1207"}

  

Scan Report

 Once your initiated scan is completed, you can easily see your
executive scan report on the build result window.

 ![](https://wiki.jenkins.io/download/attachments/175211006/JenkinsSSS.PNG?version=1&modificationDate=1569842690000&api=v2){width="1207"}

## Requirements

In order to use the Acunetix 360 scan plugin, following requirements
needs to be satisfied:

-   The user must have API token which has permission to start security
    scan.

-   The token belongs to the Acunetix 360 account must have at least one
    registered website. 

## User Guide

Acunetix 360 Jenkins Plugin documentation is always available at:

<https://www.acunetix.com/> (Will be updated)

Acunetix 360 SDLC documentation is always available at:

<https://www.acunetix.com/> (Will be updated)
