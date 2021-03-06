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

package de.topobyte.git.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

import org.eclipse.jgit.api.CherryPickResult;
import org.eclipse.jgit.api.CherryPickResult.CherryPickStatus;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import de.topobyte.git.utils.commands.CommandFactory;

public class RewriteHistory
{

	private Git git;
	private String sourceBranchName;
	private String targetBranchName;
	private CommandFactory commandFactory = null;
	private boolean forceAmendFirst = false;
	private boolean useNative = false;
	private Function<String, String> commitMessageChanger = null;

	public RewriteHistory(Git git, String sourceBranchName,
			String targetBranchName, boolean forceAmendFirst)
	{
		this.git = git;
		this.sourceBranchName = sourceBranchName;
		this.targetBranchName = targetBranchName;
		this.forceAmendFirst = forceAmendFirst;
	}

	public void setCommand(CommandFactory commandFactory)
	{
		this.commandFactory = commandFactory;
	}

	public void setUseNative(boolean useNative)
	{
		this.useNative = useNative;
	}

	public void setCommitMessageChanger(
			Function<String, String> commitMessageChanger)
	{
		this.commitMessageChanger = commitMessageChanger;
	}

	public void run() throws IOException, GitAPIException
	{
		Repository repository = git.getRepository();

		boolean found = false;
		List<Ref> refs = git.branchList().call();
		for (Ref ref : refs) {
			if (ref.getName().endsWith("/" + targetBranchName)) {
				found = true;
			}
		}

		ObjectId master = repository.resolve(sourceBranchName);
		Iterable<RevCommit> commits = git.log().add(master).call();

		List<RevCommit> list = new ArrayList<>();
		for (RevCommit commit : commits) {
			list.add(commit);
		}
		Collections.reverse(list);

		RevCommit first = list.remove(0);

		if (found) {
			System.out.println("Target branch already exists");
			return;
		}

		System.out.println("Creating empty branch");
		Ref ref = git.checkout().setName(targetBranchName).setOrphan(true)
				.call();

		System.out.println("Ref: " + ref);
		System.out.println();

		System.out.println("Resetting to first commit");
		print(first);
		git.reset().setMode(ResetType.HARD).setRef(first.name()).call();

		boolean amendFirst = false;
		if (commandFactory != null) {
			boolean successful = executeCommand(first);
			if (!successful) {
				System.out.println("Command failed");
				return;
			}
			git.add().addFilepattern(".").call();
			Status status = git.status().call();
			// We amend if something changed due to the command execution
			if (!status.isClean()) {
				amendFirst = true;
			}
		}
		if (amendFirst || forceAmendFirst) {
			amend(first);
		}

		System.out.println("Applying other commits");
		for (int i = 0; i < list.size(); i++) {
			RevCommit commit = list.get(i);
			System.out.println(String.format("PICKING %s", commit.getName()));
			print(commit);
			if (useNative) {
				boolean success = nativeCherryPick(commit.getName());
				if (!success) {
					System.out.println("cherry pick failed");
					break;
				}
			} else {
				CherryPickResult result = git.cherryPick().include(commit)
						.call();
				CherryPickStatus status = result.getStatus();
				if (status != CherryPickStatus.OK) {
					System.out.println(status);
					break;
				}
			}

			if (commandFactory != null) {
				boolean successful = executeCommand(commit);
				if (!successful) {
					System.out.println("Command failed");
					return;
				}
			}
			git.add().addFilepattern(".").call();

			amend(commit);
		}
	}

	private boolean executeCommand(RevCommit commit)
	{
		List<String> command = commandFactory.getCommand(commit);
		try {
			ProcessBuilder pb = new ProcessBuilder().command(command)
					.directory(git.getRepository().getWorkTree());
			Process p = pb.start();
			int exitValue = p.waitFor();
			return exitValue == 0;
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			return false;
		}
	}

	private boolean nativeCherryPick(String name)
	{
		List<String> command = Arrays.asList("git", "cherry-pick", name);
		try {
			ProcessBuilder pb = new ProcessBuilder().command(command)
					.directory(git.getRepository().getWorkTree());
			Process p = pb.start();
			int exitValue = p.waitFor();
			return exitValue == 0;
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			return false;
		}
	}

	public void amend(RevCommit commit) throws IOException, GitAPIException
	{
		String message = commit.getFullMessage();
		if (commitMessageChanger != null) {
			message = commitMessageChanger.apply(message);
		}
		git.commit().setAmend(true).setAuthor(commit.getAuthorIdent())
				.setCommitter(commit.getCommitterIdent()).setMessage(message)
				.call();
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
