package de.topobyte.git.utils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jgit.api.DiffCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
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

public class GitUtil
{

	public static Set<Path> collectFiles(Repository repository,
			RevCommit commit) throws MissingObjectException,
			IncorrectObjectTypeException, CorruptObjectException, IOException
	{
		Set<Path> files = new HashSet<>();

		RevTree tree = commit.getTree();
		try (TreeWalk treeWalk = new TreeWalk(repository)) {
			treeWalk.reset(tree);
			treeWalk.setRecursive(true);
			while (treeWalk.next()) {
				String path = treeWalk.getPathString();
				files.add(Paths.get(path));
			}
		}

		return files;
	}

	public static Set<Path> collectDirectories(Repository repository,
			RevCommit commit) throws MissingObjectException,
			IncorrectObjectTypeException, CorruptObjectException, IOException
	{
		Set<Path> directories = new HashSet<>();

		RevTree tree = commit.getTree();
		try (TreeWalk treeWalk = new TreeWalk(repository)) {
			treeWalk.reset(tree);
			treeWalk.setRecursive(true);
			while (treeWalk.next()) {
				String path = treeWalk.getPathString();
				Path file = Paths.get(path);
				Path parent = file.getParent();
				if (parent != null) {
					directories.add(parent);
					addParents(directories, parent);
				}
			}
		}

		return directories;
	}

	private static void addParents(Set<Path> directories, Path directory)
	{
		Path parent = directory.getParent();
		while (parent != null) {
			directories.add(parent);
			parent = parent.getParent();
		}
	}

	public static Set<Path> touchedFiles(Git git, RevCommit commit,
			RevCommit previous)
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

	public static Set<Path> touchedDirectories(Git git, RevCommit commit,
			RevCommit previous)
			throws IncorrectObjectTypeException, IOException, GitAPIException
	{
		Set<Path> directories = new HashSet<>();

		Repository repository = git.getRepository();

		DiffCommand diff = git.diff();
		diff.setOldTree(new CanonicalTreeParser(null,
				repository.newObjectReader(), previous.getTree()));
		diff.setNewTree(new CanonicalTreeParser(null,
				repository.newObjectReader(), commit.getTree()));
		List<DiffEntry> changes = diff.call();
		for (DiffEntry change : changes) {
			ChangeType type = change.getChangeType();
			switch (type) {
			case COPY:
			case ADD: {
				Path path = Paths.get(change.getNewPath());
				directories.add(path.getParent());
				break;
			}
			case DELETE: {
				Path path = Paths.get(change.getOldPath());
				directories.add(path.getParent());
				break;
			}
			case RENAME: {
				Path path1 = Paths.get(change.getOldPath());
				Path path2 = Paths.get(change.getNewPath());
				directories.add(path1.getParent());
				directories.add(path2.getParent());
				break;
			}
			case MODIFY: {
				// nothing to do here
				break;
			}
			}
		}

		return directories;
	}

}
