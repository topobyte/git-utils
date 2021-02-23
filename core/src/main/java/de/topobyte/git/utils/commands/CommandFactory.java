package de.topobyte.git.utils.commands;

import java.util.List;

import org.eclipse.jgit.revwalk.RevCommit;

public interface CommandFactory
{

	public List<String> getCommand(RevCommit commit);

}
