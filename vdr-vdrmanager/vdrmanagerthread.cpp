/*
 * VdrmonThread
 */
#include <string.h>
#include <vdr/plugin.h>
#include <vdr/thread.h>
#include <openssl/ssl.h>

#include "vdrmanagerthread.h"
#include "select.h"
#include "helpers.h"

cVdrManagerThread::cVdrManagerThread(int port, const char * password, bool forceCheckSvdrp,
                                     bool useSSL, const char * pemFile)
{
  select = NULL;
  this->port = port;
  this->password = password;
  this->forceCheckSvdrp = forceCheckSvdrp;
  this->useSSL = useSSL;
  this->pemFile = pemFile;
}

cVdrManagerThread::~cVdrManagerThread()
{
  Cleanup();
}

void cVdrManagerThread::Action(void)
{
  // initialize SSL
  if (useSSL) {
    InitSSL();
  }

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
  if (sock == NULL || !sock->Create(port, password, forceCheckSvdrp, useSSL, pemFile))
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

bool cVdrManagerThread::InitSSL() {

  SSL_library_init();
  SSL_load_error_strings();
  ERR_load_BIO_strings();
  ERR_load_SSL_strings();

}
