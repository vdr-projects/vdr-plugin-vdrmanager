/*
 * extendes sockets
 */

#ifndef _VDRMON_SOCK
#define _VDRMON_SOCK

#include <sys/types.h>
#include <sys/socket.h>
#include <string>
#include <openssl/ssl.h>

using namespace std;

class cVdrmanagerSocket
{
protected:
  int sock;
  const char * password;
  bool forceCheckSvdrp;
  bool useSSL;
protected:
  cVdrmanagerSocket();
  bool IsPasswordSet();
public:
  virtual ~cVdrmanagerSocket();
  void Close();
  int GetSocket();
  bool MakeDontBlock();
  const char * GetPassword();
  void LogSSLError();
};

class cVdrmanagerClientSocket : public cVdrmanagerSocket
{
private:
  string readbuf;
  string writebuf;
  bool disconnected;
  int client;
  bool login;
  SSL * sslContext;
public:
  cVdrmanagerClientSocket(const char * password);
  virtual ~cVdrmanagerClientSocket();
  bool Attach(int fd);
  bool IsLineComplete();
  bool GetLine(string& line);
  bool PutLine(string line);
  bool Read();
  bool Disconnected();
  void Disconnect();
  bool Flush();
  int GetClientId();
  bool WritePending();
  bool IsLoggedIn();
  void SetLoggedIn();
  bool InitSSL(SSL_CTX * sslContext);
};

class cVdrmanagerServerSocket : public cVdrmanagerSocket
{
private:
  SSL_CTX * sslContext;
public:
  cVdrmanagerServerSocket();
  virtual ~cVdrmanagerServerSocket();
  bool Create(int port, const char * password, bool forceCheckSvdrp, bool useSSL, const char * pemFile);
  cVdrmanagerClientSocket * Accept();
  bool InitSSL(const char * pemFile);
};

#endif
