# mikrotik-java
Java interface to access Mikrotik RouterOS API

## Classes to use
Here are two general packages:
- com.shakshin.mikrotik.client
- com.shakshin.mikrotik.session

The fisrt package contains low-level implemetation for RouterOS API access:
- ApiSentence - the elementary API unit used to communicate between router and client
- Connection - connection implementation around regular and SSL sockets

The second package provides high-level implementation which uses classes above and wraps them with more friendly interface.
- Request - class to build request sentence for RouterOS in easy way
- Response - class with parsed response sentences
- Session - class with session implementation around low-level connection, supports reconnecting

Yiou are free to use any level of implementation.
