package de.topobyte.git.utils.cli;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;

import de.topobyte.git.utils.SetFileModificationDates;

public class RunSetFileModificationDates
{

	public static void main(String[] args)
			throws IOException, NoHeadException, GitAPIException
	{
		if (args.length != 1) {
			System.out.println(
					"Usage: set-file-modification-dates <git directory>");
			System.exit(1);
		}

		Path repoPath = Paths.get(args[0]);

		SetFileModificationDates task = new SetFileModificationDates(repoPath);
		task.execute();
	}

}
