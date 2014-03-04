# Publishing gretty artifacts

Gretty sources are configured for publishing gretty artifacts to sonatype and bintray.
Publishing typically consists of two steps: 

1. Define publishing-specific properties 
2. Invoke publishing task. 

Below are the detailed instructions.

## Publishing to sonatype

1. Create GPG signing key, add it to your public key ring.
2. Publish public GPG key to public keyserver, for example, to keyserver.ubuntu.com
3. Create file "gradle.properties" in ~/.gradle, insert the following content to it:
  ```gradle
  signing.keyId={gpg-key-id}
  signing.password={gpg-key-passphrase}
  signing.secretKeyRingFile=/home/yourUserName/.gnupg/secring.gpg
  sonatypeUsername={sonatype-username}
  sonatypePassword={sonatype-password}
  ```    
  (Please substitute real values instead of curly braces).

4. Run from command line:
```shell
gradle uploadArchives -PpublishToSonatype=true
```

## Publishing to bintray

1. Upload private and public armored GPG key at https://bintray.com/profile/edit, section "GPG Signing".

2. Tick the checkbox "Sign this repository's files with key from: ..." at https://bintray.com/yourUserName/maven/edit?tab=general

3. Create file "gradle.properties" in ~/.gradle, insert the following content to it:
  ```groovy
  bintrayUser={bintray-user-name}
  bintrayKey={bintray-API-key}
  ```
  (Please substitute real values instead of curly braces).

4. Run from command line:
```shell
gradle bintrayUpload
```

