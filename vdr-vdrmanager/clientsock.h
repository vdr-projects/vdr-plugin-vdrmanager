/*
 * extendes sockets
 */

#ifndef _VDRMON_CLIENTSOCK
#define _VDRMON_CLIENTSOCK

#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <openssl/ssl.h>
#include <string>

#include "sock.h"

using namespace std;

class cVdrmanagerClientSocket : public cVdrmanagerSocket
{
private:
  string readbuf;
  string writebuf;
  char * sendbuf;
  size_t sendsize;
  size_t sendoffset;
  bool disconnected;
  bool initDisconnect;
  int client;
  bool login;
  bool compression;
  bool initCompression;
  int compressionMode;
  SSL * ssl;
  int sslReadWrite;
  int sslWantsSelect;
public:
  cVdrmanagerClientSocket(const char * password, int compressionMode);
  virtual ~cVdrmanagerClientSocket();
  bool Attach(int fd, SSL_CTX * sslCtx);
  bool IsLineComplete();
  bool GetLine(string& line);
  void Write(string line);
  bool Read();
  int ReadNoSSL();
  int ReadSSL();
  bool Disconnected();
  void Disconnect();
  bool Flush();
  int FlushNoSSL();
  int FlushSSL();
  int GetClientId();
  bool WritePending();
  bool IsLoggedIn();
  void SetLoggedIn();
  void ActivateCompression();
  void Compress();
  int GetSslReadWrite();
  int GetSslWantsSelect();
  bool IsSSL();
};

#endif
