/*
 * vdrmon.c: A plugin for the Video Disk Recorder
 *
 * See the README file for copyright information and how to reach the author.
 *
 * $Id$
 */

#include <vdr/plugin.h>
#include <vdr/thread.h>
#include <vdr/status.h>
#include <vdr/device.h>
#include <vdr/player.h>
#include "vdrmanagerthread.h"
#include "compressor.h"

#define VDRMANAGER_PORT		6420

static const char *VERSION = "0.9";
static const char *DESCRIPTION = "VDR-Manager support plugin";

class cVdrManager: public cPlugin {
private:
	// Add any member variables or functions you may need here.
	cVdrManagerThread * Thread;
	int port;
	const char * password;
	bool forceCheckSvdrp;
	bool forceDelete;
	int compressionMode;
protected:
public:
	cVdrManager(void);
	virtual ~cVdrManager();
	virtual const char *Version(void) {
		return VERSION;
	}
	virtual const char *Description(void) {
		return DESCRIPTION;
	}
	virtual const char *CommandLineHelp(void);
	virtual bool Initialize(void);
	virtual bool Start(void);
	virtual void Stop(void);
	virtual void Housekeeping(void);
	virtual const char *MainMenuEntry(void) {
		return NULL;
	}
	virtual cOsdObject *MainMenuAction(void);
	virtual cMenuSetupPage *SetupMenu(void);
	virtual bool ProcessArgs(int argc, char *argv[]);
};

cVdrManager::cVdrManager(void) {
	// Initialize any member variables here.
	// DON'T DO ANYTHING ELSE THAT MAY HAVE SIDE EFFECTS, REQUIRE GLOBAL
	// VDR OBJECTS TO EXIST OR PRODUCE ANY OUTPUT!
	Thread = NULL;
	port = VDRMANAGER_PORT;
	password = "";
	forceCheckSvdrp = false;
	forceDelete = false;
}

cVdrManager::~cVdrManager() {
	// Clean up after yourself!
}

cOsdObject * cVdrManager::MainMenuAction(void) {
	return NULL;
}

cMenuSetupPage * cVdrManager::SetupMenu(void) {
	return NULL;
}

const char * cVdrManager::CommandLineHelp(void) {
	return
	    "  -p port          port number to listen to\n"
	    "  -P password      password (none if not given). No password forces check against svdrphosts.conf.\n"
	    "  -s               force check against svdrphosts.conf, even if a password was given\n"
	    "  -f               force delete of a timer or a recording even if they are active\n"
	    "  -c compression   selects the compression mode to use (zlib, gzip or none)";
}

bool cVdrManager::ProcessArgs(int argc, char *argv[]) {
	int c;
	while ((c = getopt(argc, argv, "c:p:P:sf")) != -1)
		switch (c) {
		case 'p':
			port = atoi(optarg);
			break;
		case 'P':
			password = optarg;
			break;
		case 's':
			forceCheckSvdrp = true;
			break;
		case 'f':
			forceDelete = true;
			break;
		case 'c':
		  if (optarg[0] == 'g') {
		    compressionMode = COMPRESSION_GZIP;
		  } else if (optarg[0] == 'z') {
		    compressionMode = COMPRESSION_ZLIB;
		  } else {
		    compressionMode = COMPRESSION_NONE;
		  }
		  break;
		case '?':
			return false;
		default:
			return false;
		}

// default port
	if (port <= 0)
		port = VDRMANAGER_PORT;

	return true;
}

bool cVdrManager::Initialize(void) {
// Initialize any background activities the plugin shall perform.

// Start any background activities the plugin shall perform.
	Thread = new cVdrManagerThread(port, password, forceCheckSvdrp, compressionMode);

	return Thread != NULL;
}

bool cVdrManager::Start(void) {
	Thread->Start();

	return true;
}

void cVdrManager::Stop(void) {
// Stop any background activities the plugin shall perform.
	Thread->Shutdown();
}

void cVdrManager::Housekeeping(void) {
// Perform any cleanup or other regular tasks.
}

VDRPLUGINCREATOR(cVdrManager);
// Don't touch this!
