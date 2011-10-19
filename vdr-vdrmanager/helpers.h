/*
 * helper tools
 */

#include <time.h>
#include <string>
#include <vdr/epg.h>

using namespace std;

class cHelpers
{
public:
  static string GetTimers(string args);
  static string GetChannels(string args);
  static string GetChannelEvents(string args);
  static string GetTimeEvents(string args);
  static string GetRecordings(string args);
  static string SetTimer(string args);
  static string SearchEvents(string args);
  static string ToUpper(string text);
  static string ToLower(string text);
  static string Trim(string text);
private:
  static string SafeCall(string (*)());
  static string SafeCall(string (*)(string), string arg);
  static string SafeCall(string (*)(string, string), string arg1, string arg2);
  static string GetTimersIntern();
  static string GetRecordingsIntern();
  static string GetChannelsIntern(string wantedChannels);
  static string GetEventsIntern(string wantedChannels, string when);
  static string SetTimerIntern(string args);
  static string SearchEventsIntern(string wantedChannels, string pattern);
  static bool IsWantedEvent(cEvent * event, string pattern);
  static bool IsWantedChannel(cChannel * channel, string wantedChannels);
  static bool IsWantedTime(time_t when, cEvent * event);
  static string MapSpecialChars(string text);
  static string ToText(const cEvent * event);
  static string ToText(cTimer * timer);
  static string ToText(cRecording * recording);
};
