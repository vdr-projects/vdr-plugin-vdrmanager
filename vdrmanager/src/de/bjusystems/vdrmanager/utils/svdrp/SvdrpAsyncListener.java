package de.bjusystems.vdrmanager.utils.svdrp;

public interface SvdrpAsyncListener<Result> extends SvdrpListener<Result> {

	void svdrpException(SvdrpException exception);

}
