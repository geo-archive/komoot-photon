Submit a new issue only if you are sure it is a missing feature or a bug. Otherwise
please discuss the topic in the
[discussion section](https://github.com/komoot/photon/discussions/) first.

## We love pull requests. Here's a quick guide:

1. [Fork the repo](https://help.github.com/articles/fork-a-repo)
and create a branch for your new feature or bug fix.

2. Run the tests. We only take pull requests with passing tests: `./gradlew build`

3. Add at least one test for your change. Only refactoring and documentation changes
require no new tests. Also make sure you submit a change specific to exactly
one issue. If you have ideas for multiple 
changes please create separate pull requests.

4. Make the test(s) pass.

5. Push to your fork and [submit a pull request](https://help.github.com/articles/using-pull-requests).

## Code formatting

We use IntelliJ defaults. For eclipse there is this
[configuration](https://github.com/graphhopper/graphhopper/files/481920/GraphHopper.Formatter.zip).
Also for other IDEs it should be simple to match:

 * Java indent is 4 spaces, no tabs
 * Line width is 100 characters
 * The rest is left to Java coding standards but disable "auto-format on save" to prevent unnecessary format changes.
 * Currently we do not care about import section that much, avoid changing it
 * Unix line endings (should be handled via git)

And in case we didn't emphasize it enough: we love tests!

## Changes to mapping and index settings

If the mappings or the settings are changed in an incompatible way that
requires a reimport, then you must increase the database version in
`src/main/java/de/komoot/photon/Server.java`. See instructions on how
to increase the version in the file for more information.
