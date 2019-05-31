package de.topobyte.git.utils;

import java.io.IOException;
import java.nio.file.Path;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;

import de.topobyte.system.utils.SystemPaths;

public class TestSetFileModificationDates
{

	public static void main(String[] args)
			throws IOException, NoHeadException, GitAPIException
	{
		Path repoPath = SystemPaths.HOME.resolve("github/topobyte/jts-utils");

		SetFileModificationDates task = new SetFileModificationDates(repoPath);
		task.execute();
	}

}
