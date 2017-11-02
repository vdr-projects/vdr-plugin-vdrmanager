package de.bjusystems.vdrmanager.data;

/**
 * @author lado
 *
 *         Based on epg.c from vdr
 *
 */
public interface EventContentGroup {

	static int MovieDrama = 0x10; //
	static int NewsCurrentAffairs = 0x20; //
	static int Show = 0x30; //
	static int Sports = 0x40; //
	static int ChildrenYouth = 0x50; //
	static int MusicBalletDance = 0x60; //
	static int ArtsCulture = 0x70; //
	static int SocialPoliticalEconomics = 0x80;//
	static int EducationalScience = 0x90;//
	static int LeisureHobbies = 0xA0;//
	static int Special = 0xB0; //
	static int UserDefined = 0xF0;//
}