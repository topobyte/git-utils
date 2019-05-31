package de.topobyte.git.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

public class SetFileModificationDates
{

	private Path repoPath;

	public SetFileModificationDates(Path repoPath)
	{
		this.repoPath = repoPath;
	}

	private Set<Path> currentFiles;
	private Set<Path> todo;
	private Map<Path, Date> fileToDate = new HashMap<>();

	public void execute() throws IOException, NoHeadException, GitAPIException
	{
		Git git = Git.open(repoPath.toFile());
		analyzeCommits(git);

		for (Path path : currentFiles) {
			System.out.println(
					String.format("%s %s", fileToDate.get(path), path));
			Path absoluteFile = repoPath.resolve(path);
			Date date = fileToDate.get(path);
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

		RevCommit first = commits.get(0);
		currentFiles = GitUtil.collectFiles(repository, first);
		todo = new HashSet<>(currentFiles);

		RevCommit last = null;

		for (RevCommit commit : commits) {
			if (last == null) {
				last = commit;
				continue;
			}

			Set<Path> touched = GitUtil.touchedFiles(git, last, commit);
			update(commit, touched);

			if (todo.isEmpty()) {
				return;
			}

			last = commit;
		}

		Set<Path> filesInitial = GitUtil.collectFiles(repository, last);
		update(last, filesInitial);

		git.close();
	}

	private void update(RevCommit commit, Set<Path> touched)
	{
		Date time = commit.getCommitterIdent().getWhen();

		for (Path file : touched) {
			if (todo.contains(file)) {
				todo.remove(file);
				fileToDate.put(file, time);
			}
		}
	}

}
