package de.topobyte.git.utils;

import java.nio.file.Path;
import java.util.Comparator;

public class PathComparatorByLength implements Comparator<Path>
{

	@Override
	public int compare(Path p1, Path p2)
	{
		int nc1 = p1 == null ? 0 : p1.getNameCount();
		int nc2 = p2 == null ? 0 : p2.getNameCount();
		return Integer.compare(nc1, nc2);
	}

}
