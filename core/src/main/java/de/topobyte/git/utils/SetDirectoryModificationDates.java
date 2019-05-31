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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import de.topobyte.collections.util.ListUtil;

public class SetDirectoryModificationDates
{

	private Path repoPath;

	public SetDirectoryModificationDates(Path repoPath)
	{
		this.repoPath = repoPath;
	}

	private Set<Path> currentDirectories;
	private Map<Path, Date> dirToDate = new HashMap<>();

	public void execute() throws IOException, NoHeadException, GitAPIException
	{
		Git git = Git.open(repoPath.toFile());
		analyzeCommits(git);

		Date rootDate = dirToDate.get(null);
		System.out.println(String.format("%s %s", rootDate, "."));
		Files.setLastModifiedTime(repoPath,
				FileTime.from(rootDate.toInstant()));

		for (Path path : currentDirectories) {
			System.out
					.println(String.format("%s %s", dirToDate.get(path), path));
			Path absoluteFile = repoPath.resolve(path);
			Date date = dirToDate.get(path);
			Files.setLastModifiedTime(absoluteFile,
					FileTime.from(date.toInstant()));
		}
	}

	private void analyzeCommits(Git git)
			throws IOException, NoHeadException, GitAPIException
	{
		Repository repository = git.getRepository();

		List<RevCommit> commits = new ArrayList<>();
		git.log().call().forEach(commits::add);

		if (commits.isEmpty()) {
			return;
		}

		Collections.reverse(commits);

		RevCommit first = commits.get(0);
		RevCommit last = ListUtil.last(commits);

		Set<Path> initialDirectories = GitUtil.collectDirectories(repository,
				first);
		currentDirectories = GitUtil.collectDirectories(repository, last);

		Date timeFirst = first.getCommitterIdent().getWhen();
		dirToDate.put(null, timeFirst);
		for (Path directory : initialDirectories) {
			dirToDate.put(directory, timeFirst);
		}

		for (int i = 1; i < commits.size(); i++) {
			RevCommit commit = commits.get(i);
			RevCommit previous = commits.get(i - 1);

			Set<Path> touched = GitUtil.touchedDirectories(git, commit,
					previous);
			List<Path> sorted = new ArrayList<>(touched);
			Collections.sort(sorted, new PathComparatorByLength());
			for (Path path : sorted) {
				Date date = commit.getCommitterIdent().getWhen();
				boolean isNew = !dirToDate.containsKey(path);
				dirToDate.put(path, date);
				if (path != null) {
					// touch non-existing parent directories
					touchParents(path, date);
					if (isNew) {
						// if this is a new directory, update the direct parent
						touchParent(path, date);
					}
				}
			}
		}

		git.close();
	}

	private void touchParent(Path directory, Date date)
	{
		Path parent = directory.getParent();
		dirToDate.put(parent, date);
	}

	private void touchParents(Path directory, Date date)
	{
		Path parent = directory.getParent();
		while (parent != null) {
			if (dirToDate.containsKey(parent)) {
				return;
			}
			dirToDate.put(parent, date);
			parent = parent.getParent();
		}
	}

}
