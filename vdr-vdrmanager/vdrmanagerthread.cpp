/*
 * VdrmonThread
 */
#include <string.h>
#include <vdr/plugin.h>
#include <vdr/thread.h>
#include "vdrmanagerthread.h"
#include "select.h"
#include "helpers.h"

cVdrManagerThread::cVdrManagerThread(int port, const char * password, bool forceCheckSvdrp, int compressionMode)
{
  select = NULL;
  this -> port = port;
  this -> password = password;
  this -> forceCheckSvdrp = forceCheckSvdrp;
  this -> compressionMode = compressionMode;
}

cVdrManagerThread::~cVdrManagerThread()
{
  Cleanup();
}

void cVdrManagerThread::Action(void)
{
  // create listener socket
  if (!Init())
    return;

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
  if (sock == NULL || !sock->Create(port, password, forceCheckSvdrp, compressionMode))
    return false;

  // register server socket
  select->SetServerSocket(sock);

  return true;
}

void cVdrManagerThread::Cleanup()
{
  if (select)
    delete select;
}

void cVdrManagerThread::Shutdown()
{
}
