package de.bjusystems.vdrmanager.utils.svdrp;

public interface SvdrpExceptionListener {
	void svdrpEvent(SvdrpEvent event, Throwable t);
}
