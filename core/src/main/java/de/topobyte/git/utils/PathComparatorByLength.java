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
