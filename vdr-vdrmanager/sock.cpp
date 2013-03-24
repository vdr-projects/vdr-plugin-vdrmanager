/*
 * extendes sockets
 */
#include <unistd.h>
#include <vdr/plugin.h>
#include "sock.h"
#include "helpers.h"
#include "compressor.h"

static int clientno = 0;

/*
 * cVdrmonSocket
 */
cVdrmanagerSocket::cVdrmanagerSocket() {
	sock = -1;
}

cVdrmanagerSocket::~cVdrmanagerSocket() {
	Close();
}

void cVdrmanagerSocket::Close() {
	if (sock >= 0) {
		close(sock);
		sock = -1;
	}
}

int cVdrmanagerSocket::GetSocket() {
	return sock;
}

bool cVdrmanagerSocket::MakeDontBlock() {
	// make it non-blocking:
	int oldflags = fcntl(sock, F_GETFL, 0);
	if (oldflags < 0) {
		LOG_ERROR;
		return false;
	}
	oldflags |= O_NONBLOCK;
	if (fcntl(sock, F_SETFL, oldflags) < 0) {
		LOG_ERROR;
		return false;
	}

	return true;
}

const char * cVdrmanagerSocket::GetPassword() {
	return password;
}

/*
 * cVdrmonServerSocket
 */
cVdrmanagerServerSocket::cVdrmanagerServerSocket() :
		cVdrmanagerSocket() {
}

cVdrmanagerServerSocket::~cVdrmanagerServerSocket() {
}

bool cVdrmanagerServerSocket::Create(int port, const char * password, bool forceCheckSvrp, int compressionMode) {

  this->password = password;
	this->forceCheckSvdrp = forceCheckSvrp;
	this->compressionMode = compressionMode;

	// create socket
	sock = socket(PF_INET, SOCK_STREAM, 0);
	if (sock < 0) {
		LOG_ERROR;
		return false;
	}

	// allow it to always reuse the same port:
	int ReUseAddr = 1;
	setsockopt(sock, SOL_SOCKET, SO_REUSEADDR, &ReUseAddr, sizeof(ReUseAddr));

	// bind to address
	struct sockaddr_in name;
	name.sin_family = AF_INET;
	name.sin_port = htons(port);
	name.sin_addr.s_addr = htonl(INADDR_ANY);
	if (bind(sock, (struct sockaddr *) &name, sizeof(name)) < 0) {
		LOG_ERROR;
		Close();
		return false;
	}

	// make it non-blocking:
	if (!MakeDontBlock()) {
		Close();
		return false;
	}

	// listen to the socket:
	if (listen(sock, 100) < 0) {
		LOG_ERROR;
		Close();
		return false;
	}

	return true;
}

cVdrmanagerClientSocket * cVdrmanagerServerSocket::Accept() {
	cVdrmanagerClientSocket * newsocket = NULL;

	// accept the connection
	struct sockaddr_in clientname;
	uint size = sizeof(clientname);
	int newsock = accept(sock, (struct sockaddr *) &clientname, &size);
	if (newsock > 0) {
		// create client socket
		newsocket = new cVdrmanagerClientSocket(password, compressionMode);
		if (!newsocket->Attach(newsock)) {
			delete newsocket;
			return NULL;
		}

		if (!IsPasswordSet() || forceCheckSvdrp == true) {
			bool accepted = SVDRPhosts.Acceptable(clientname.sin_addr.s_addr);
			if (!accepted) {
				newsocket->PutLine(string("NACC Access denied.\n"));
				newsocket->Flush();
				delete newsocket;
				newsocket = NULL;
			}
			dsyslog(
					"[vdrmanager] connect from %s, port %hd - %s", inet_ntoa(clientname.sin_addr), ntohs(clientname.sin_port), accepted ? "accepted" : "DENIED");
		}
	} else if (errno != EINTR && errno != EAGAIN
		)
		LOG_ERROR;

	return newsocket;
}

/*
 * cVdrmonClientSocket
 */
cVdrmanagerClientSocket::cVdrmanagerClientSocket(const char * password, int compressionMode) {
	readbuf = "";
	writebuf = "";
	sendbuf = NULL;
	sendsize = 0;
	sendoffset = 0;
	disconnected = false;
	initDisconnect = false;
	client = ++clientno;
	this->password = password;
	this->compressionMode = compressionMode;
	login = false;
	compression = false;
	initCompression = false;
}

cVdrmanagerClientSocket::~cVdrmanagerClientSocket() {
}

bool cVdrmanagerClientSocket::IsLineComplete() {
	// check a for complete line
	string::size_type pos = readbuf.find("\r", 0);
	if (pos == string::npos)
		pos = readbuf.find("\n");
	return pos != string::npos;
}

bool cVdrmanagerSocket::IsPasswordSet(){
	return strcmp(password, "");
}

bool cVdrmanagerClientSocket::GetLine(string& line) {
	// check the line
	string::size_type pos = readbuf.find("\r", 0);
	if (pos == string::npos)
		pos = readbuf.find("\n", 0);
	if (pos == string::npos)
		return false;

	// extract the line ...
	line = readbuf.substr(0, pos);

	// handle \r\n
	if (readbuf[pos] == '\r' && readbuf.length() > pos
			&& readbuf[pos + 1] == '\n')
		pos++;

	// ... and move the remainder
	readbuf = readbuf.substr(pos + 1);

	return true;
}

bool cVdrmanagerClientSocket::Read() {
	if (Disconnected())
		return false;

	int rc;
	bool len = 0;
	char buf[2001];
	while ((rc = read(sock, buf, sizeof(buf) - 1)) > 0) {
		buf[rc] = 0;
		readbuf += buf;
		len += rc;
	}

	if (rc < 0 && errno != EAGAIN)
	{
		LOG_ERROR;
		return false;
	} else if (rc == 0) {
		disconnected = true;
	}

	return len > 0;
}

bool cVdrmanagerClientSocket::Disconnected() {
	return disconnected;
}

void cVdrmanagerClientSocket::Disconnect() {
	initDisconnect = true;
}

bool cVdrmanagerClientSocket::PutLine(string line) {

  // fill writebuf
  if (line.length() > 0) {
    writebuf += line;
    return true;
  }

	// initialize sendbuf if needed
  if (sendbuf == NULL) {
    if (!compression) {
      sendbuf = (char *)malloc(writebuf.length()+1);
      strcpy(sendbuf, writebuf.c_str());
      sendsize = writebuf.length();
    } else {
      Compress();
    }
    sendoffset = 0;
    writebuf.clear();
  }

  // send data
  if (sendsize > 0) {

		// write so many bytes as possible
		int rc = write(sock, sendbuf + sendoffset, sendsize);
		if (rc < 0 && errno != EAGAIN)
		{
			LOG_ERROR;

			if (sendbuf != NULL) {
			  free(sendbuf);
			  sendbuf = NULL;
			}

			return false;
		}
		sendsize -= rc;
		sendoffset += rc;
	}

  if (sendsize == 0) {

    if (sendbuf != NULL) {
      free(sendbuf);
      sendbuf = NULL;
    }

	  if (initCompression) {
	    isyslog("Compression is activated now");
	    initCompression = false;
	    compression = true;
	  }

	  if (initDisconnect) {
	    initDisconnect = false;
	    disconnected = true;
	  }
	}

	return true;
}

bool cVdrmanagerClientSocket::Flush() {
	return PutLine("");
}

bool cVdrmanagerClientSocket::Attach(int fd) {
	sock = fd;
	return MakeDontBlock();
}

int cVdrmanagerClientSocket::GetClientId() {
	return client;
}

bool cVdrmanagerClientSocket::WritePending() {
	return sendoffset < sendsize;
}

bool cVdrmanagerClientSocket::IsLoggedIn() {
	return login || !password || !*password;
}

void cVdrmanagerClientSocket::SetLoggedIn() {
	login = true;
}

void cVdrmanagerClientSocket::ActivateCompression() {

  string mode = "NONE";
  switch (compressionMode) {
  case COMPRESSION_GZIP:
    mode = "GZIP";
    initCompression = true;
    break;
  case COMPRESSION_ZLIB:
    mode = "ZLIB";
    initCompression = true;
    break;
  default:
    mode = "NONE";
    break;
  }

  PutLine("!OK " + mode + "\r\n");
}

void cVdrmanagerClientSocket::Compress() {
  cCompressor compressor = cCompressor();

  switch (compressionMode) {
  case COMPRESSION_GZIP:
    compressor.CompressGzip(writebuf);
    break;
  case COMPRESSION_ZLIB:
    compressor.CompressZlib(writebuf);
    break;
  }

  sendbuf = compressor.GetData();
  sendsize = compressor.getDataSize();

  double ratio = 1.0 * writebuf.length() / sendsize;
  isyslog("Compression stats: raw %ld, compressed %ld, ratio %f:1", writebuf.length(), sendsize, ratio);
}
