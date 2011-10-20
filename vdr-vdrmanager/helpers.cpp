
/*
 * event und message handler
 */

#include <time.h>
#include <unistd.h>
#include <values.h>
#include <vdr/plugin.h>
#include <vdr/timers.h>
#include <vdr/recording.h>
#include <vdr/channels.h>
#include <vdr/epg.h>
#include <vdr/videodir.h>
#include "helpers.h"
#include "vdrmanagerthread.h"

string cHelpers::GetRecordings(string args) {
  return SafeCall(GetRecordingsIntern);
}

string cHelpers::GetTimers(string args) {
  return SafeCall(GetTimersIntern);
}

string cHelpers::GetChannels(string args) {
  return SafeCall(GetChannelsIntern, args);
}

string cHelpers::GetChannelEvents(string args) {
  return SafeCall(GetEventsIntern, Trim(args), "");
}

string cHelpers::GetTimeEvents(string args) {

  args = Trim(args);

  size_t space = args.find(' ');
  if (space == string::npos) {
    return SafeCall(GetEventsIntern, "", args);
  }

  string when = args.substr(0, space);
  string wantedChannels = args.substr(space+1);

  return SafeCall(GetEventsIntern, Trim(wantedChannels), Trim(when));
}

string cHelpers::SetTimer(string args) {
  return SafeCall(SetTimerIntern, args);
}

string cHelpers::SearchEvents(string args) {

  args = Trim(args);

  size_t space = args.find(' ');
  if (space == string::npos) {
    return "!ERROR\r\n";
  }

  string wantedChannels = args.substr(0, space);
  string pattern = args.substr(space+1);

  return SafeCall(SearchEventsIntern, Trim(wantedChannels), Trim(pattern));

}

string cHelpers::GetTimersIntern() {

  string result = "START\r\n";
  
  // iterate through all timers
  for(cTimer * timer = Timers.First(); timer; timer = Timers.Next(timer)) {
      result += ToText(timer);
  }

  return result + "END\r\n";
}

string cHelpers::GetRecordingsIntern() {

  string result = "START\r\n";
  // iterate through all recordings
  cRecording* recording = NULL;
  for (int i = 0; i < Recordings.Count(); i++) {
    recording = Recordings.Get(i);
    result += ToText(recording);
  }
  return result + "END\r\n";
}

string cHelpers::GetChannelsIntern(string wantedChannels) {

  string result = "START\r\n";
  string currentGroup = "";

  char number[10];
  for(cChannel * channel = Channels.First(); channel; channel = Channels.Next(channel)) {

    // channel group
    if (channel->GroupSep()) {
      currentGroup = channel->Name();
      continue;
    }

    // channel filtering
    if (IsWantedChannel(channel, wantedChannels)) {
      // current group
      if (currentGroup.length() > 0) {
        result += "C0:";
        result += currentGroup;
        result += "\r\n";
        currentGroup = "";
      }

      // channel
      sprintf(number, "C%d", channel->Number());
      result += number;
      result += ":";
      result += channel->Name();
      result += ":";
      result += channel->Provider();
      result += "\r\n";
    }
  }

  return result + "END\r\n";
}

string cHelpers::GetEventsIntern(string wantedChannels, string when) {

  when = ToUpper(when);
  time_t wantedTime;
  if (when == "NOW" || when == "NEXT") {
    wantedTime = time(0);
  } else {
    wantedTime = atol(when.c_str());
  }

  string result = "START\r\n";

  cSchedulesLock schedulesLock;
  const cSchedules * schedules = cSchedules::Schedules(schedulesLock);
  for(cSchedule * schedule = schedules->First(); schedule; schedule = schedules->Next(schedule)) {

    cChannel * channel = Channels.GetByChannelID(schedule->ChannelID());
    if (!IsWantedChannel(channel, wantedChannels)) {
      continue;
    }

    const cList<cEvent> * events = schedule->Events();
    for(cEvent * event = events->First(); event; event = events->Next(event)) {
      if (IsWantedTime(wantedTime, event)) {
        cEvent * match = event;
        if (when == "NEXT") {
          match = events->Next(match);
          if (!match) {
            break;
          }
        }

        result += ToText(match);

        if (when.length() > 0) {
          break;
        }
      }
    }
  }

  return result + "END\r\n";
}

string cHelpers::SetTimerIntern(string args) {

  // separete timer number
  size_t sep = args.find(':');
  if (sep == string::npos) {
    return "!ERROR:no separator found\r\n";
  }
  
  char c =  args[0];

  string numberstr = args.substr(sep-1,1);

  int number = atoi(numberstr.c_str());
  
  string params = args.substr(sep+1);
 
  
  // parse timer
  cTimer * timer = new cTimer;
  if (!timer->Parse(params.c_str())) {
    delete timer;
    return "!ERROR:can not parse params '"+params+"'\r\n";
  }
  
  cTimer * oldTimer;
  switch(c){
  case 'C':     // new timer
  case 'c':
    Timers.Add(timer);
    break;
  case 'D':
  case 'd':
    // delete timer
    delete timer;
    oldTimer = Timers.Get(number);
    Timers.Del(oldTimer, true);
    break;
  case 'M':
  case 'm':
    // modify
    oldTimer = Timers.Get(number);
    oldTimer->Parse(params.c_str());
    break;
  default:
    return "!ERROR:unknown timer command\r\n";
  }

  Timers.Save();

  return "START\r\nEND\r\n";
}


string cHelpers::SearchEventsIntern(string wantedChannels, string pattern) {

  string result = "START\r\n";

  cSchedulesLock schedulesLock;
  const cSchedules * schedules = cSchedules::Schedules(schedulesLock);
  for(cSchedule * schedule = schedules->First(); schedule; schedule = schedules->Next(schedule)) {

    cChannel * channel = Channels.GetByChannelID(schedule->ChannelID());
    if (!IsWantedChannel(channel, wantedChannels)) {
      continue;
    }

    const cList<cEvent> * events = schedule->Events();
    for(cEvent * event = events->First(); event; event = events->Next(event)) {

      if (IsWantedEvent(event, pattern)) {
        result += ToText(event);
      }
    }
  }

  return result + "END\r\n";
}

string cHelpers::ToText(cRecording * recording){
  const cRecordingInfo * info = recording->Info();
  const cEvent * event = info->GetEvent();
  
  /**
  tChannelID ChannelID(void) const;
  const cSchedule *Schedule(void) const { return schedule; }
  tEventID EventID(void) const { return eventID; }
  uchar TableID(void) const { return tableID; }
  uchar Version(void) const { return version; }
  int RunningStatus(void) const { return runningStatus; }
  const char *Title(void) const { return title; }
  const char *ShortText(void) const { return shortText; }
  const char *Description(void) const { return description; }
  const cComponents *Components(void) const { return components; }
  uchar Contents(int i = 0) const { return (0 <= i && i < MaxEventContents) ? contents[i] : 0; }
  int ParentalRating(void) const { return parentalRating; }
  time_t StartTime(void) const { return startTime; }
  time_t EndTime(void) const { return startTime + duration; }
  int Duration(void) const { return duration; }
  time_t Vps(void) const { return vps; }
  time_t Seen(void) const { return seen; }
  bool SeenWithin(int Seconds) const { return time(NULL) - seen < Seconds; }
  bool HasTimer(void) const;
  bool IsRunning(bool OrAboutToStart = false) const;
  static const char *ContentToString(uchar Content);
  cString GetParentalRatingString(void) const;
  cString GetDateString(void) const;
  cString GetTimeString(void) const;
  cString GetEndTimeString(void) const;
  cString GetVpsString(void) const;
  */

  char buf[100];
  string result = "";
  
  time_t startTime = event->StartTime();
  time_t endTime = event->EndTime();

  sprintf(buf, "%lu", startTime);
  result += buf;
  result += ":";
  sprintf(buf, "%lu", endTime);
  result += buf;
  result += ":";
  sprintf(buf, "%d", DirSizeMB(recording->FileName()));
  result += buf;
  result += ":";
  result += info -> ChannelName();
  result += ":";
  result += MapSpecialChars(event->Title());
  result += ":";
  result += MapSpecialChars(event->ShortText() ? event->ShortText() : "");
  result += ":";
  result += MapSpecialChars(event->Description() ? event->Description() : "");
  result += ":";
  result += recording->FileName();
  result += "\r\n";
  return result;
}

string cHelpers::ToText(cTimer * timer) {
  
  const cChannel * channel = timer->Channel();
  const char * channelName = channel->Name();
  
  cSchedulesLock schedulesLock;
  const cSchedules * schedules = cSchedules::Schedules(schedulesLock);
  
  const cSchedule * schedule = schedules->GetSchedule(channel->GetChannelID());
  
  const cList<cEvent> * events = schedule->Events();
  cEvent * match = NULL;
  for(cEvent * event = events->First(); event; event = events->Next(event)) {
    
    time_t startTime = event->StartTime();
    time_t stopTime = startTime + event->Duration();
    if(startTime <= timer->StartTime() && timer->StopTime() >= stopTime){
      match = event;
      break;
    }
  }
  
  string result;
  char buf[100];
  sprintf(buf, "T%d", timer->Index());
  result = buf;
  result += ":";
  sprintf(buf, "%u", timer->Flags());
  result += buf;
  result += ":";
  sprintf(buf, "%d", timer->Channel()->Number());
  result += buf;
  result += ":";
  result += channelName;
  result += ":";
  sprintf(buf, "%lu", timer->StartTime());
  result += buf;
  result += ":";
  sprintf(buf, "%lu", timer->StopTime());
  result += buf;
  result += ":";
  sprintf(buf, "%d", timer->Priority());
  result += buf;
  result += ":";
  sprintf(buf, "%d", timer->Lifetime());
  result += buf;
  result += ":";
  result += MapSpecialChars(timer->File());
  result += ":";
  result += MapSpecialChars(timer->Aux() ? timer->Aux() : "");
  if(match && false){
    result += ":";
    result += MapSpecialChars(match->ShortText()  ? match->ShortText() : "");
    result += ":";
    result += MapSpecialChars(match->Description() ? match->Description() : "");
  } else {
    result += "::";
  }
  result += "\r\n";

  return result;
}

string cHelpers::ToText(const cEvent * event) {


  cChannel * channel = Channels.GetByChannelID(event->Schedule()->ChannelID());

  // search assigned timer
  cTimer * eventTimer = NULL;
  for(cTimer * timer = Timers.First(); timer; timer = Timers.Next(timer)) {
    if (timer->Channel() == channel && timer->StartTime() <= event->StartTime() &&
        timer->StopTime() >= event->StartTime() + event->Duration()) {
      eventTimer = timer;
    }
  }

  char buf[100];
  string result;
  sprintf(buf, "E%d", channel->Number());
  result = buf;
  result += ":";
  result += channel->Name();
  result += ":";
  sprintf(buf, "%lu", event->StartTime());
  result += buf;
  result += ":";
  sprintf(buf, "%lu", event->StartTime() + event->Duration());
  result += buf;
  result += ":";
  result += MapSpecialChars(event->Title());
  result += ":";
  result += MapSpecialChars(event->Description() ? event->Description() : "");
  result += ":";
  result += MapSpecialChars(event->ShortText() ? event->ShortText() : "");
  result += "\r\n";

  if (eventTimer) {
    result += ToText(eventTimer);
  }

  return result;
}

bool cHelpers::IsWantedEvent(cEvent * event, string pattern) {

  string text = event->Title();
  if (event->Description()) {
    text += event->Description();
  }

  return ToLower(text).find(ToLower(pattern)) != string::npos;
}

bool cHelpers::IsWantedChannel(cChannel * channel, string wantedChannels) {

  if (!channel) {
    return false;
  }

  if (wantedChannels.length() == 0) {
    return true;
  }

  int number = channel->Number();
  const char * delims = ",;";
  char * state;
  char * buffer = (char *)malloc(wantedChannels.size()+1);
  strcpy(buffer, wantedChannels.c_str());

  bool found = false;
  for(char * token = strtok_r(buffer, delims, &state); token; token = strtok_r(NULL, delims, &state)) {
    const char * rangeSep = strchr(token, '-');
    if (rangeSep == NULL) {
      // single channel
      if (atoi(token) == number) {
        found = true;
      }
    } else {
      // channel range
      int start = atoi(token);
      while (*rangeSep && *rangeSep == '-')
        rangeSep++;
      int end = *rangeSep ? atoi(rangeSep) : INT_MAX;

      if (start <= number && number <= end) {
        found = true;
      }
    }
  }
  return found;
}

bool cHelpers::IsWantedTime(time_t when, cEvent * event) {

  time_t startTime = event->StartTime();
  time_t stopTime = startTime + event->Duration();

  if (when == 0) {
    return stopTime >= time(0);
  }

  return startTime <= when && when < stopTime;
}

string cHelpers::ToUpper(string text)
{
  for(unsigned i = 0; i < text.length(); i++)
  {
    if (islower(text[i]))
      text[i] = toupper(text[i]);
  }

  return text;
}

string cHelpers::ToLower(string text)
{
  for(unsigned i = 0; i < text.length(); i++)
  {
    if (isupper(text[i]))
      text[i] = tolower(text[i]);
  }

  return text;
}


string cHelpers::Trim(string text) {

  const char * start = text.c_str();

  // skip leading spaces
  const char * first = start;
  while (*first && isspace(*first))
    first++;

  // find trailing spaces
  const char * last = first + strlen(first) - 1;
  while (first < last && isspace(*last))
    last--;

  char * dst = (char *)malloc(last - first + 2);
  sprintf(dst, "%*s", last - first + 1, first);

  return dst;
}

string cHelpers::SafeCall(string (*f)())
{
  // loop, if vdr modified list and we crash
  for (int i = 0; i < 3; i++)
  {
    try
    {
      return f();
    }
    catch (...)
    {
      usleep(100);
    }
  }

  return "";
}

string cHelpers::SafeCall(string (*f)(string arg), string arg)
{
  // loop, if vdr modified list and we crash
  for (int i = 0; i < 3; i++)
  {
    try
    {
      return f(arg);
    }
    catch (...)
    {
      usleep(100);
    }
  }

  return "";
}


string cHelpers::SafeCall(string (*f)(string arg1, string arg2), string arg1, string arg2)
{
  // loop, if vdr modified list and we crash
  for (int i = 0; i < 3; i++)
  {
    try
    {
      return f(arg1, arg2);
    }
    catch (...)
    {
      usleep(100);
    }
  }

  return "";
}

string cHelpers::MapSpecialChars(string text) {

  const char * p = text.c_str();
  string result = "";
  while (*p) {
    switch (*p) {
    case ':':
      result += "|##";
      break;
    case '\r':
      break;
    case '\n':
      result += "||#";
      break;
    default:
      result += *p;
      break;
    }
    p++;
  }
  return result;
}
