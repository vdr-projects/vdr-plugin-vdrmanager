/*
 * VdrmonThread
 */
#include <string.h>
#include <vdr/plugin.h>
#include <vdr/thread.h>
#include "vdrmanagerthread.h"
#include "select.h"
#include "helpers.h"

cVdrManagerThread::cVdrManagerThread(int port, int sslPort, const char * password, bool forceCheckSvdrp, int compressionMode,
                                     const char * certFile, const char * keyFile)
{
  select = NULL;
  this->port = port;
  this->sslPort = sslPort;
  this->password = password;
  this->forceCheckSvdrp = forceCheckSvdrp;
  this->compressionMode = compressionMode;
  this->certFile = certFile;
  this->keyFile = keyFile;
}

cVdrManagerThread::~cVdrManagerThread()
{
  Cleanup();
}

void cVdrManagerThread::Action(void)
{
  // create listener socket
  if (!Init()) {
    Cleanup();
    return;
  }

  // do processing
  select->Action();

  // cleanup
  Cleanup();
}

bool cVdrManagerThread::Init()
{
  // create select
  select = new cSelect();
  if (select == NULL)
    return false;

  // create server socket
  cVdrmanagerServerSocket * sock = new cVdrmanagerServerSocket();
  if (sock == NULL || !sock->Create(port, password, forceCheckSvdrp, compressionMode, NULL, NULL))
    return false;

  // register server sockets
  select->SetServerSockets(sock, NULL);

  cVdrmanagerServerSocket * sslSock;
  if (!access(certFile, R_OK) && !access(keyFile, R_OK))  {
    sslSock = new cVdrmanagerServerSocket();
    if (sslSock == NULL || !sslSock->Create(sslPort, password, forceCheckSvdrp, compressionMode, certFile, keyFile)) {
      return false;
    }
  } else {
    sslSock = NULL;
    isyslog("[vdrmanager] SSL key files %s and %s can't be read. SSL disabled.", certFile, keyFile);
  }

  // register server sockets
  select->SetServerSockets(sock, sslSock);

  return true;
}

void cVdrManagerThread::Cleanup()
{
  if (select) {
    delete select;
    select = NULL;
  }
}

void cVdrManagerThread::Shutdown()
{
}
