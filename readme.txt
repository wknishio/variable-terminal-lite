# Variable-Terminal

## Overview

This software is a java remote computer administration tool.

It has various functions inspired by TELNET, RLOGIN, SSH, NETBUS, VNC,
but its incompatible with those tools/protocols/standards.

This is the lite variant for restricted environments as android.

Some of the available features are:

* Support for ISAAC, VMPC, SALSA, HC or ZUC encryption.
* Support for UPnP, NAT-PMP and PCP NAT port forwarding.
* Support for SOCKS and HTTP proxy network connections.
* Support for LZ4 and ZSTD data compression.
* Multiple simultaneous sessions.
* Limited native process creation and control.
* Alternative integrated beanshell remote shell.
* Both client and server can be run in background
* Automatic client reconnection after disconnection.
* Adjustable text font size.
* Simple text messaging between client and server.
* File transfer with compression and resume/synchronization.
* Remote screen capture.
* Remote desktop view and control.
* Remote clipboard control.
* Multiple display support.
* Audio chat communication between client and server.
* Network tunneling with SOCKS/HTTP/FTP/TCP tunnels and compression.
* Network usage rate limiter.
* Network latency verification.
* Remote popup alerts.
* Remote browser opening.
* Remote mail client opening.
* Remote printing of texts and files.
* Remote audio playback.
* Remote disc tray control.
* Record client commands to text file.
* Execute client commands from text file.
* Record client console output to text file.

The minimum required java version to execute is at least 1.5.
This software comes with no warranty, use at your own risk!

## Installation and initialization

To install this software, just extract the contents of the released zip file
in any folder.

There are 4 main modes of operation for this software:

* Client module to control instances of server module.
* Server module to be controlled by instances of client module.
* Agent module is the client module in background without user interface.
* Daemon module is the server module in background without user interface.

In user interface 2 options are available:

* Graphical console needs a graphical environment but have connection
configuration dialogs and helper menus for commands.  
* Shell console uses the native shell of the operating system and can be used
in environments without graphical interface but with a text terminal.

The default and recommended user interface is the graphical console, as most
computers nowadays have support for graphical environments.

The included scripts using format vate-[mode]-[interface].[*] can
be used to start this software used in a specific combination of mode and user
interface.

## Connection settings

To start running effectively, connection settings must be set for client
and server, when using graphical console, a dialog will appear to ease the
configuration of connection settings.

The connection settings are these:

* Connection mode controls which instance will attempt to connect and which
instance will listen for connections, as in a TCP connection.  
Active connection mode indicates that it will attempt to connect, it is the
default connection mode of the client.  
Passive connection mode indicates that it will listen for connections, it is
the default connection mode of the server.  
To connect successfully, one instance must be in active connection mode while
the other must be in passive connection mode.
* Connection host controls which TCP host address will be used for connection.
* Connection port controls which TCP port will be used for connection, default
TCP port is 6060.
* Connection NAT port sets a NAT port mapping using UPnP or NAT-PMP protocols.
* Proxy type enables support for connections using HTTP or SOCKS proxies.
* Proxy host controls the TCP host address of the proxy.
* Proxy port controls the TCP port of the proxy.
* Proxy user controls the user for proxy authentication.
* Proxy password controls the password for proxy authentication.
* Encryption type enables connection encryption using encryption algorithms.
* Encryption password sets the password for connection encryption.
* Ping limit controls the connection ping timeout in milliseconds.
* Ping interval controls the connection ping interval in milliseconds.
* Session shell sets the command to be used as remote shell in server.
* Session commands set commands separated by *; to be run when session starts.
* Session maximum define a limit for simultaneous sessions in server.
* Session user sets the session user for session.
* Session password sets the session password for session.

All connection settings can be set using the files  
"vate-client.properties" for client instances and  
"vate-server.properties" for server instances,  
those files support UTF-8 encoding.

The connection settings can also be set using program arguments at startup,
these are the available program arguments:

* -H: list parameters
* -C: use client module, -A: use agent module
* -S: use server module, -D: use daemon module
* -LF: load settings file
* -CM: connection mode, passive(P), active(A)
* -CH: connection host, default null
* -CP: connection port, default 6060
* -CN: connection NAT port, default null
* -PT: proxy type, default none, DIRECT(D), SOCKS(S), HTTP(H), PLUS(P)
* -PH: proxy host, default null
* -PP: proxy port, default 1080 for SOCKS or default 8080 for HTTP
* -PU: proxy user, default null
* -PK: proxy password, default null
* -ET: encryption type, default none/ISAAC(I)/VMPC(R)/SALSA(S)/HC(H)/ZUC(Z)
* -EK: encryption password, default null
* -PL: ping limit, default 60000 milliseconds
* -PI: ping interval, default 15000 milliseconds
* -SS: session shell, default null
* -SC: session commands, separated by "*;", default null, only in client
* -SM: session maximum, default 0, only in server
* -SU: session user, default null
* -SK: session password, default null

## Console commands

This software is primarily a command-line tool driven by text commands.

After successfully connecting and authenticating to a server, the client
console can receive text command inputs from the user.

When the user types a command in client and press enter, this software checks
if the command must be redirected to the remote shell running on the server or
if the command is a internal command of this software.

Most of the functions of this software are executed by internal commands.

After configuring the server, the server console can also receive text command
inputs, but only internal commands of this software.

When using graphical console, the helper menu "Command" will be available,
assisting the use of all internal commands of this software.

Some of the available internal commands in client console:

* Commands *VTFILETRANSFER or *VTFT do file transfers.
* Commands *VTSCREENSHOT or *VTSS do remote screen captures.
* Commands *VTGRAPHICSLINK or *VTGL toggle remote desktop control.
* Commands *VTAUDIOLINK or *VTAL toggle audio chat communication.
* Commands *VTTUNNEL or *VTTN set network connection tunnels.
* Commands *VTLIMIT or *VTLM limit network connection data rates.

There are more internal commands in client console and server console:

* Commands *VTHELP or *VTHL show a list of available internal commands.
* Commands *VTHELP [COMMAND] or *VTHL [CMD] show more details about
specific internal commands.

## Building from sources

The minimum required java version to build from sources is at least JDK 1.5.

The included scripts "build.bat" and "build.sh" perform the build process
using ant and the file "build.xml" is an ant build file for this software.

After running the ant build file, the packaged application distributions can
be found in the "dist" folder.

## Used libraries

Those are the third party libraries used in this software:

* MindTerm by AppGate, for circular buffer pipe stream
* JZlib by Atsuhiko Yamanaka, for pure java zlib compression
* jpountz lz4-java by Adrien Grand, for lz4 compression and xxhash checksum
* Java Native Access by Todd Fast/Timothy Wall/Liang Chen, for native calls
* jsocks by Kirill Kouzoubov/Robert Simac, for SOCKS proxy tunneling
* JSAP by Martian Software, for command parsing
* PngEncoder by ObjectPlanet, for faster PNG image encoding
* ARGBPixelGrabber by pumpernickel, for image data extraction
* UPNPLib by sbbi, for UPnP NAT port forwarding support
* TomP2P by Thomas Bocek, for NAT-PMP NAT port forwarding support
* JSpeex by Horizon Wimba, for Speex audio communication
* Concentus Java by Logan Stromberg, for Opus audio communication
* client-side Throttle by James Edwards, for network data rate limiter
* DirectRobot by Killer99 from rune-server.ee, for better screen capture
* PortMapper by Kasra Faghihi(offbynull) for PCP port forwarding support
* Lanterna by mabe02 for graphical console
* Sixlegs Java PNG Decoder for PNG decoding
* bouncycastle by Legion of the Bouncy Castle for encryption
* beanshell2 by pejobo for alternative shell
* airlift-aircompressor by Martin Traverso for zstd and lzo compression
* nanohttpd-1.1 by elonen, for HTTP proxy tunneling
* commons-httpclient by Apache Software Foundation, for HTTP proxy client
* commons-rng by Apache Software Foundation, for splitmix64 prng
* Base85 by Sheep-y, for backported Base85 encoder/decoder
* PngEncoder by Looklet, for better PNG image encoding
* MinimalFTP by Guilherme Chaguri, for FTP server tunneling

## Additional utilities

Some additional utilities are included with distributions:

* beanshell2 by pejobo
* autologon for windows
* lockstation for windows
* logon for windows
* srvany for windows
* uac for windows

## License

This software is under MIT license

Copyright (c) AD 2020 William Kendi Nishio

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.