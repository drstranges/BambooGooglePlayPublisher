# Bamboo Google Play Publisher

## Overview

Plugin for Atlassian Bamboo for uploading apk files on Google Play using Publishing API.

## Build
* atlas-run   -- installs this plugin into the product and starts it on localhost
* atlas-debug -- same as atlas-run, but allows a debugger to attach at port 5005
* atlas-cli   -- after atlas-run or atlas-debug, opens a Maven command line window:
                 - 'pi' reinstalls the plugin into the running product instance
* atlas-help  -- prints description for all commands in the SDK

## Used Library:
  - [Google Play Developer API client library](https://developers.google.com/android-publisher)

## Data security and privacy statement
This plugin does not collect any data despite the task configuration. The task configuration will be saved in your bamboo database. The plugin will not download or upload any data to third party companies, except files of your app that you select to publish on Google Play - this files uploads directly to Google Play. You can also check the source code to prove this statement and/or compile the plugin from source if you do not trust atlassian market place.

License
=======

    Copyright 2016 Roman Donchenko

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
