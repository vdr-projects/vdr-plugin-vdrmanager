/*
 * VdrmonThread
 */

#ifndef _VDRMON_THREAD
#define _VDRMON_THREAD

#include <sys/poll.h>
#include <vdr/plugin.h>
#include <vdr/thread.h>
#include <vdr/device.h>
#include <vdr/player.h>
#include <string>

using namespace std;

class cSelect;

class cVdrManagerThread : public cThread {
private:
  cSelect * select;
  int port;
  const char * password;
  bool forceCheckSvdrp;
  int compressionMode;
public:
  cVdrManagerThread(int port, const char * password, bool forceCheckSvdrp, int compressionMode);
  virtual void Action(void);
  void Shutdown();
private:
  virtual ~cVdrManagerThread();
  void Cleanup();
  bool Init();
};

#endif


