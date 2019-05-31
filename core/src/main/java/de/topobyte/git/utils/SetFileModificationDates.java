package de.topobyte.git.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jgit.api.DiffCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;

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
		currentFiles = collectFiles(repository, first);
		todo = new HashSet<>(currentFiles);

		RevCommit last = null;

		for (RevCommit commit : commits) {
			if (last == null) {
				last = commit;
				continue;
			}

			Set<Path> touched = touched(git, last, commit);
			update(commit, touched);

			if (todo.isEmpty()) {
				return;
			}

			last = commit;
		}

		Set<Path> filesInitial = collectFiles(repository, last);
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

	private Set<Path> collectFiles(Repository repository, RevCommit first)
			throws MissingObjectException, IncorrectObjectTypeException,
			CorruptObjectException, IOException
	{
		Set<Path> files = new HashSet<>();

		RevTree treeFirst = first.getTree();
		try (TreeWalk treeWalk = new TreeWalk(repository)) {
			treeWalk.reset(treeFirst);
			treeWalk.setRecursive(true);
			while (treeWalk.next()) {
				String path = treeWalk.getPathString();
				files.add(Paths.get(path));
			}
		}

		return files;
	}

	private Set<Path> touched(Git git, RevCommit commit, RevCommit previous)
			throws IncorrectObjectTypeException, IOException, GitAPIException
	{
		Set<Path> files = new HashSet<>();

		Repository repository = git.getRepository();

		DiffCommand diff = git.diff();
		diff.setOldTree(new CanonicalTreeParser(null,
				repository.newObjectReader(), previous.getTree()));
		diff.setNewTree(new CanonicalTreeParser(null,
				repository.newObjectReader(), commit.getTree()));
		List<DiffEntry> changes = diff.call();
		for (DiffEntry change : changes) {
			ChangeType type = change.getChangeType();
			if (type == ChangeType.DELETE) {
				continue;
			}
			files.add(Paths.get(change.getNewPath()));
		}

		return files;
	}

}
