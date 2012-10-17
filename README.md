MicroWebServer
==============
This is a fork and improvement of a webserver I once wrote as Android Application.
The UI is for controlling (start/stop),logs,settings,etc (working on more stuff).

There is rudimentary scripting support using Z3MScript (a fork of zemscript).

The webserver itself runs as an android service. The listening socket might be forwarded to port 80 (if root ! and iptables/kernel supports this) by iptable rules.
It's yet relatively stable and supports "Connection: keep-alive" (HTTP/1.1) as well as resumeable downloads (range header).
The webroot may be configured.
Android resources can be cached and displayed (at this time drawables). Directorylisting is supported and may
be configured (on/off).

A databas manager (managa a bunch of sqlite databases with "user" rights) is included and can be interfaced through Z3M as well.

There is also the possebility to add "static path" webservices (like a REST interface).

NEW
***
"RemoteWebServices" may now be deployed into the Server using the microwebserver-messagebinder library. Any App or Service may add "services" which can be called via http :).
For an simple example take a look at microwebserver-remotetest. Please note, that these remote services must first be enabled inside the Microwebserver App (services tab),
the enabled state is not yet saved, so you'll hav to do this manually every time a service is "deployed".
This feature is somewhat experimental although the example may work fine, expect changes to the microwebserver-messagebinder.

Future plans are to add custom webroots for remote services to be able to serve files or scripts as well. Also some broadcast may be added to provide a stable lifecycle to remote services
(if the server is not running the interfacing app should not expect input pver http and vise versa)
***


PHP support *could* possibly be added through SL4A (https://code.google.com/p/android-scripting/ , http://phpforandroid.net/), although it might not be very useful because of missing mysql/gd/.. ?!?
I will do some "researches" for what might be possible. 

The following projects are used/referenced within the code
***

- json-fork (packe containing renamed org.json classes [for android compability reasons], source will be added soon)
- sqldroid (https://code.google.com/p/sqldroid/)
- Z3Mscript (or zemfork,[https://github.com/ScR4tCh/Z3MScript] a fork of ZemScript https://code.google.com/p/zemscript/)
- android file chooser (also a fork of an little android lib : https://github.com/ScR4tCh/Android-File-Chooser)
- viepager indicator (http://viewpagerindicator.com/)
- BASE64 class by Robert Harder (http://iharder.net/base64)
- Fork of mime-utils "magic header detection" (http://sourceforge.net/projects/mime-util/)

(missing references and credits will follow !)

This App is licensed under The LGPL.
