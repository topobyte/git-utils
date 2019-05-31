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
