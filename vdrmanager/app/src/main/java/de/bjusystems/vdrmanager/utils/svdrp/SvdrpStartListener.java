package de.bjusystems.vdrmanager.utils.svdrp;

public interface SvdrpStartListener {
	/**
	 *
	 * START is read
	 *
	 * The Start line may contain addition information separated by '|'
	 *
	 * So START|some meta info
	 * @param meta may be null
	 */
	void start(String meta);

}
