# NeoProtect plugin (DEPRECATED, DO NOT USE)

A new plugin is currently in development and will be released here soon.

## Feature

| Feature                                                             |     Available      |
|:--------------------------------------------------------------------|:------------------:|
| Proxy Protocol                                                      | :white_check_mark: |
| Debug-tool for high ping problem (BungeeCord, Velocity coming soon) | :white_check_mark: |
| Auto update backend IP                                              | :white_check_mark: |
| Anti-portscanner (Bungeecord + Velocity)                            | :white_check_mark: |
| IPanic mode command (toggle AntiBot level)                          | :white_check_mark: |
| Some commands to interact with NeoProtect intern system             | :white_check_mark: |
| In-game message if server is under attack                           | :white_check_mark: |
| In-game analytics                                                   | :white_check_mark: |

Prerequisites:
=================

1) You have created an account and possess a GameShield at [NeoProtect](https://neoprotect.net).
2) You have a server/network that you can connect to.
3) The required software installed [Compatibility version](https://github.com/NeoProtect/NeoPlugin/blob/master/SECURITY.md)
 
Install Instructions:
=====================

1) First, stop your server/network and put the plugin jar into the plugins folder.
   Continue by starting your server and note that if there is a folder in the plugins folder called "NeoProtect",
   don't change anything there.

2) Now join the server while having the permission "neoprotect.admin" when joining
   and you should now see a message in the chat telling you how to proceed.

# Permissions

You have to set the permission ``neoprotect.admin`` before you can start the setup.

| Command / Feature    | Description                                                 | Permission                             |
|:---------------------|-------------------------------------------------------------|----------------------------------------|
| /np setup            | start the setup (setup set API-KEY, backend and gameshield) | neoprotect.admin                       |
| /np ipanic           | toggle AntiBot level                                        | neoprotect.admin                       |
| /np toggle (option)  | toggle different options                                    | neoprotect.admin                       |
| /np debugTool        | start debug tool                                            | neoprotect.admin                       |
| /np setgameshield    | set gameshield for establish the connection to NeoProtect   | neoprotect.admin                       |
| /np setbackend       | set backend for establish the connection to NeoProtect      | neoprotect.admin                       |
| under attack message | In-game message if server is under attack                   | neoprotect.admin<br/>neoprotect.notify |

## Take a look at the config.yml

With the /neoprotect setup command, all config settings, apart from 'Language:' and 'autoUpdateIP:', are set automatically and should not normally be changed yourself.

```
# Don't change anything here if you don't know what you're doing
APIKey: '' # The API-KEY is set automatically during setup
ProxyProtocol: true # Needed to forward the player's IP through the NeoProtect Proxy
Language: en-US # en-US or de-DE (Add new file to /language for more available language https://www.oracle.com/java/technologies/javase/jdk8-jre8-suported-locales.html)
gameshield:
  serverId: '' # The serverID is set automatically during setup
  backendId: '' # The backendID is set automatically during setup
  autoUpdateIP: false # This setting automatically sets the IP of the NeoProtect backend every 10 seconds
DebugMode: false # IMPORTANT Please ensure that you activate this option solely upon receiving a prompt from a NeoProtect maintainer.
# This setting is only for paid costumer and allow you to disable the AutoUpdater
# 'ENABLED'  (Recommended/Default) Update/Downgrade plugin to the current version  
# 'DISABLED' AutoUpdater just disabled
# 'DEV'      Only update to the latest version (Please never use this)
AutoUpdater: ENABLED
```
