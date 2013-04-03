/*
 * extendes sockets
 */

#ifndef _VDRMON_SOCK
#define _VDRMON_SOCK

#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <string>

using namespace std;

class cVdrmanagerSocket
{
protected:
  int sock;
  const char * password;
  bool forceCheckSvdrp;
  int compressionMode;
protected:
  cVdrmanagerSocket();
  bool IsPasswordSet();
public:
  virtual ~cVdrmanagerSocket();
  void Close();
  int GetSocket();
  bool MakeDontBlock();
  const char * GetPassword();
};

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
public:
  cVdrmanagerClientSocket(const char * password, int compressionMode);
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
  void ActivateCompression();
  void Compress();
};

class cVdrmanagerServerSocket : public cVdrmanagerSocket
{
public:
  cVdrmanagerServerSocket();
  virtual ~cVdrmanagerServerSocket();
  bool Create(int port, const char * password, bool forceCheckSvdrp, int compressionMode);
  cVdrmanagerClientSocket * Accept();
};

#endif
