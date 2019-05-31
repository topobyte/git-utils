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
import java.util.Date;
import java.util.List;

import org.eclipse.jgit.api.DiffCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;

import de.topobyte.system.utils.SystemPaths;

public class TestCommitFiles
{

	public static void main(String[] args)
			throws IOException, NoHeadException, GitAPIException
	{
		System.out.println("Test");
		Path repoPath = SystemPaths.HOME.resolve("github/topobyte/jts-utils");

		Git git = Git.open(repoPath.toFile());
		Repository repository = git.getRepository();

		Iterable<RevCommit> commits = git.log().call();

		RevCommit last = null;

		for (RevCommit commit : commits) {
			if (last == null) {
				last = commit;
				continue;
			}

			diff(git, last, commit);

			last = commit;
		}

		print(last);
		RevTree tree = last.getTree();
		try (TreeWalk treeWalk = new TreeWalk(repository)) {
			treeWalk.reset(tree);
			while (treeWalk.next()) {
				String path = treeWalk.getPathString();
				System.out.println(path);
			}
		}

		git.close();
	}

	private static void diff(Git git, RevCommit commit, RevCommit previous)
			throws IncorrectObjectTypeException, IOException, GitAPIException
	{
		Repository repository = git.getRepository();

		print(commit);

		DiffCommand diff = git.diff();
		diff.setOldTree(new CanonicalTreeParser(null,
				repository.newObjectReader(), previous.getTree()));
		diff.setNewTree(new CanonicalTreeParser(null,
				repository.newObjectReader(), commit.getTree()));
		List<DiffEntry> changes = diff.call();
		System.out.println("changes: " + changes.size());
		for (DiffEntry change : changes) {
			ChangeType type = change.getChangeType();
			System.out.println(type + " " + change.getOldPath() + " "
					+ change.getNewPath());
		}
	}

	private static void print(RevCommit commit)
	{
		System.out.println(String.format("COMMIT %s", commit.getName()));
		Date time = commit.getCommitterIdent().getWhen();
		System.out.println(time);
		String message = commit.getFullMessage();
		System.out.println(message);
	}

}
