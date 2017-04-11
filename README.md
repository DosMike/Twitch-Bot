DosBot
=====

Description
-----

This bot was designed to join lots of features into one bot with the goal that
you, as a streamer do not have to create yet another account on a bot website.
As this may result in the bot having feature you do not like we try to write 
everything in a way that allows you to simply disable the feature in the bot's
configuration file.

A big feature of this bot is the `chat_trigger.txt`. It's a definition file
that allows you to easily create chat commands with certain properties like
cooldowns, chat-rank limitations, point-costs and other things. The definition
file is also able to limit the built-in commands. For more information on how
chat triggers work, please see the [[wiki]].

As you may only want to use this bot to add one specific feature we try to make
as much data as possible available to other bots using a telnet API. For now
the API is not yet implemented but will be fixed at localhost, port 23232 for
any bot to register command listeners, request and change user data and more.

To check if DosBot is running on a Twitch-channel you just have to call him by
typing `DosBot` in chat. If he's running he will respond to that message every
few minutes or so.

Get a copy
-----

This Bot is currently under development.

No copies will be given out to the public (yet).

License
-----

```
 DosBot
Copyright 2017 DosMike aka ITwookie

 Jackson Project
Copyright ???? FasterXML https://github.com/FasterXML/jackson/wiki/FAQ

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```