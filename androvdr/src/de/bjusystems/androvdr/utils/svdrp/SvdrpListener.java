package de.bjusystems.androvdr.utils.svdrp;


public interface SvdrpListener<Result>{

	void svdrpEvent(SvdrpEvent event, Result result);
}
