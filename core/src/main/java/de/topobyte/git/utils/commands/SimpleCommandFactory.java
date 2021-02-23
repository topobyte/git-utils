package de.topobyte.git.utils.commands;

import java.util.List;

import org.eclipse.jgit.revwalk.RevCommit;

public class SimpleCommandFactory implements CommandFactory
{

	private List<String> command;

	public SimpleCommandFactory(List<String> command)
	{
		this.command = command;
	}

	@Override
	public List<String> getCommand(RevCommit commit)
	{
		return command;
	}

}
