package de.topobyte.git.utils;

import java.nio.file.Path;
import java.util.Comparator;

public class PathComparatorByLength implements Comparator<Path>
{

	@Override
	public int compare(Path o1, Path o2)
	{
		return Integer.compare(o1.getNameCount(), o2.getNameCount());
	}

}
