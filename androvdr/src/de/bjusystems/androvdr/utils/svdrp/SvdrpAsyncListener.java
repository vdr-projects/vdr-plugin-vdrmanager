package de.bjusystems.androvdr.utils.svdrp;

public interface SvdrpAsyncListener<Result> extends SvdrpListener<Result> {

	void svdrpException(SvdrpException exception);

}
