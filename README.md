# About

This Java library provides utilities for working with Git from Java projects
and provides some functionality as a command line tool.

# License

This library is released under the terms of the GNU Lesser General Public
License.

See  [LGPL.md](LGPL.md) and [GPL.md](GPL.md) for details.

# Using the CLI

There are currently two tools:

    ./scripts/set-file-modification-dates
    ./scripts/set-directory-modification-dates

* `set-file-modification-dates`
  sets the last-modification time of each file to the commit time of the last
  commit that touched that file.
* `set-directory-modification-dates`
  sets the last-modification time of each directory to the commit time of the
  last commit that created or deleted a file in that directory.
