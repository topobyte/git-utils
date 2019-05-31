// Copyright 2019 Sebastian Kuerten
//
// This file is part of git-utils.
//
// git-utils is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// git-utils is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with git-utils. If not, see <http://www.gnu.org/licenses/>.

package de.topobyte.git.utils.cli;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;

import de.topobyte.git.utils.SetFileModificationDates;

public class RunSetDirectoryModificationDates
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
