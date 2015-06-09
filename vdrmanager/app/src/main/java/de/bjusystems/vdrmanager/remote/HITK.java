package de.bjusystems.vdrmanager.remote;

/**
 * Created by lado on 09.05.15.
 */
public enum HITK {
    Up,
    Down,
    Menu,
    Ok,
    Back,
    Left,
    Right,
    Red,
    Green,
    Yellow,
    Blue,
    Zero("0"),
    One("1"),
    Two("2"),
    Three("3"),
    Four("4"),
    Five("5"),
    Six("6"),
    Seven("7"),
    Eight("8"),
    Nine("9"),
    Info,
    Play_Pause("Play/Pause"),
    Play,
    Pause,
    Stop,
    Record,
    FastFwd,
    FastRew,
    Next,
    Prev,
    Power,
    ChannelUp("Channel+"),
    ChannelDown("Channel-"),
    PrevChannel,
    VolumeUp("Volume+"),
    VolumeDown("Volume-"),
    Mute,
    Audio,
    Subtitles,
    Schedule,
    Channels,
    Timers,
    Recordings,
    Setup,
    Commands,
    User0,
    User1,
    User2,
    User3,
    User4,
    User5,
    User6,
    User7,
    User8,
    User9,;

    String value;

    private HITK() {
        this.value = this.name();
    }

    private HITK(String value) {
        this.value = value;
    }

    public String getValue(){
        return  this.value;
    }
}
