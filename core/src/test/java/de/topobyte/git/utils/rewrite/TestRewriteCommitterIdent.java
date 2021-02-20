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

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;

import de.topobyte.git.utils.RewriteHistory;
import de.topobyte.system.utils.SystemPaths;

public class TestRewriteCommitterIdent
{

	public static void main(String[] args) throws IOException, GitAPIException
	{
		System.out.println("Rewriting History");
		Path dir = SystemPaths.HOME.resolve("git/some-repo");

		Git git = Git.open(dir.toFile());

		RewriteHistory rewrite = new RewriteHistory(git, "master", "rewrite",
				true) {

			@Override
			public void amend(RevCommit commit)
					throws IOException, GitAPIException
			{
				git.commit().setAmend(true).setAuthor(commit.getAuthorIdent())
						.setCommitter(commit.getAuthorIdent())
						.setMessage(commit.getFullMessage()).call();
			}

		};
		rewrite.run();
	}

}
