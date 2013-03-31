/*
 * compressor.h
 *
 *  Created on: 23.03.2013
 *      Author: bju
 */

#ifndef COMPRESSOR_H_
#define COMPRESSOR_H_

#include <string>

#define COMPRESSION_NONE 0
#define COMPRESSION_ZLIB 1
#define COMPRESSION_GZIP 2

using namespace std;

class cCompressor {
private:
  char * data;
  size_t size;
public:
  cCompressor();
  virtual ~cCompressor();
  bool CompressGzip(string text);
  bool CompressZlib(string text);
  char * GetData();
  size_t getDataSize();
};
#endif /* COMPRESSOR_H_ */

