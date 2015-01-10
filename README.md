quadcopter-java-server
======================

A Java server for quadcopters to connect to

Server is designed to allow Quadcopters to be controlled over the internet.
Server simply pairs a remote to a quadcopter and validates chatter between the two.

Server allows Quadcopter to disconnect from the server temporarily (suspend connection)
and reconnect after a specified time.

In order to pair, Quadcopter and remote must identify themselves with the same key.
The key should be well unique.
