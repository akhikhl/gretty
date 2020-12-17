## Running a release

- Set release version, drop `-SNAPSHOT`.
- Update all required doc files - see a previous release tag for an example of what should be updated.
- Final release commit should be signed with `-S -s`.
- Check build passes on GitHub Actions.
- Tag release using `git tag -a -s -m "release ?.?.?" v?.?.?`.
- Checkout tag.
- Set JDK path to a JDK8 installation.
- `./gradlew build`
- Export required variables.  I precede these with a space and have `HISTCONTROL=ignorespace` so they won't appear in my history:

```
 export BINTRAY_USER="javabrett"
 export BINTRAY_KEY="<secret>"
 export BINTRAY_REPO="maven"
 export BINTRAY_PACKAGE="org.gretty"
 export GPG_PASSPHRASE="<secret>"
```

- Check `~/.gradle/gradle.properties` for credentials for plugins.gradle.org:

```
gradle.publish.key=<secret>
gradle.publish.secret=<secret>
```

- Push to bintray (again I lead with a space):

```
 ./gradlew bintrayUpload -PbintrayUser=${BINTRAY_USER} -PbintrayKey=${BINTRAY_KEY} -PbintrayRepo=${BINTRAY_REPO} -PbintrayPackage=${BINTRAY_PACKAGE} -PgpgPassphrase="${GPG_PASSPHRASE}"
```

- Publish to plugins.gradle.org:

```
 ./gradlew publishPlugins
```

- Release files on Bintray - login and release stages files.
- Update a test-project to use the new Gretty version number and confirm download and build.
- Push tags: `git push origin --tags`.
- Update version on `master` to new version number with `-SNAPSHOT` suffix.
- Update version links in [README.md](README.md).
- Add/edit the release created on GitHub.
