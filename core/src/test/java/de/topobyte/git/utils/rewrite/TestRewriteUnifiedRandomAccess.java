// Copyright 2021 Sebastian Kuerten
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

package de.topobyte.git.utils.rewrite;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import de.topobyte.git.utils.RewriteHistory;
import de.topobyte.git.utils.commands.SimpleCommandFactory;
import de.topobyte.system.utils.SystemPaths;

public class TestRewriteUnifiedRandomAccess
{

	public static void main(String[] args) throws IOException, GitAPIException
	{
		System.out.println("Rewriting History");
		Path dir = SystemPaths.HOME
				.resolve("github/topobyte/unified-random-access");

		Git git = Git.open(dir.toFile());

		List<String> command = Arrays.asList("project-tools",
				"source add-headers", "--year", "2015",
				"unified-random-access");

		RewriteHistory rewrite = new RewriteHistory(git, "master", "rewrite",
				false);
		rewrite.setCommand(new SimpleCommandFactory(command));
		rewrite.run();
	}

}
