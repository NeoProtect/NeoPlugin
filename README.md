# NeoProtect plugin
Official NeoProtect plugin maintained by [EinfacheSache (aka CubeAttack)](https://github.com/EinfacheSache)


## Feature

| Feature                                                                     |      Available      |
|:----------------------------------------------------------------------------|:-------------------:|
| Proxy Protocol                                                              | :white_check_mark:  |
| Update Backend IP                                                           | :white_check_mark:  |
| Anti-portscanner (Bungeecord + Velocity)                                    | :white_check_mark:  |
| IPanic mode command (toggle AntiBot level)                                  | :white_check_mark:  |
| Some commands to interact with NeoProtect intern system                     | :white_check_mark:  |
| In-game message if under attack (player with permission 'neoprotect.admin') | :white_check_mark:  |
| In-game analytics                                                           | :hammer_and_wrench: |


Pre-Requirements:
=================
1) You have created an account with a gameShield at [NeoProtect](https://neoprotect.net).
2) You have a server/network that you can connect to.
3) [Compatibility version](https://github.com/NeoProtect/NeoPlugin/blob/master/SECURITY.md)
 
  
Install Instructions:
=====================
1) First stop your server/network and then put the plugin in the Plugins folder up.
   Then start your server now if there is a folder in the Plugins folder called "NeoProtect" 
   don't change anything there.

2) Now join the server you need the permission "neoprotect.admin" when joining, 
   and you should now see a message in the chat telling you how to proceed.


## Commands

| Command           | Description                                                 | Permission       |
|:------------------|-------------------------------------------------------------|------------------|
| /np setup         | start the setup (setup set API-KEY, backend and gameshield) | neoprotect.admin |
| /np ipanic        | toggle AntiBot level                                        | neoprotect.admin |
| /np setgameshield | set gameshield for establish the connection to NeoProtect   | neoprotect.admin |
| /np setbackend    | set backend for establish the connection to NeoProtect      | neoprotect.admin |

## Config.yml 
```
# Don't change anything here if you don't know what you're doing
APIKey: '' # The API-KEY is set automatically during setup
ProxyProtocol: true # Needed to forward the player's IP through the NeoProtect Proxy
Language: en-US # en-US or de-DE (Add new file to /language for more available language https://www.oracle.com/java/technologies/javase/jdk8-jre8-suported-locales.html)
gameshield:
  serverId: '' # The serverID is set automatically during setup
  backendId: '' # The backendID is set automatically during setup
  autoUpdateIP: false # This setting automatically sets the IP of the NeoProtect backend every 30 seconds
```
