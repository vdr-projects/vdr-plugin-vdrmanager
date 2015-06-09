package de.bjusystems.vdrmanager.utils.svdrp;

import java.util.List;

public interface SvdrpFinishedListener<Result> {

	public void finished(List<Result> results);

}
