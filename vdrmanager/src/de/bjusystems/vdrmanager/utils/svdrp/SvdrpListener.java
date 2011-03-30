package de.bjusystems.vdrmanager.utils.svdrp;


public interface SvdrpListener<Result>{

	void svdrpEvent(SvdrpEvent event, Result result);
}
