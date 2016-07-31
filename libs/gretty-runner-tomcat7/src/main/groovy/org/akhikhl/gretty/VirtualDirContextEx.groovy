package org.akhikhl.gretty

import org.apache.naming.NamingEntry
import org.apache.naming.resources.Resource
import org.apache.naming.resources.VirtualDirContext

import javax.naming.NamingException
/**
 * @author sala
 */
class VirtualDirContextEx extends VirtualDirContext {
  private static final String WEB_INF_LIB_PREFIX = "/WEB-INF/lib"

  private final Map<String, File> webInfJarMap = [:]
  private final List<File> webInfJars = []

  public void setWebInfJars(List<File> jars) {
    webInfJars.addAll(jars)
  }

  @Override
  void allocate() {
    super.allocate()
    webInfJarMap.clear()
    webInfJars.each {
      String fileName = it.name
      if(fileName.endsWith('.jar')) {
        webInfJarMap.put(fileName, it)
      }
    }
  }

  @Override
  void release() {
    super.release()
    webInfJarMap.clear()
    webInfJars.clear()
  }

  @Override
  protected List<NamingEntry> doListBindings(String name) throws NamingException {
    def listBindings = super.doListBindings(name)

    if(listBindings) {
      return listBindings
    }

    if(WEB_INF_LIB_PREFIX == name) {
      def entries = webInfJarMap.entrySet()
      def list = entries.collect {
        new NamingEntry(it.key, new FileResource(it.value), NamingEntry.ENTRY)
      }
      return list
    }

    return null
  }

  @Override
  protected File file(String name) {
    def file = super.file(name)
    if(!file || !file.exists()) {
      if(name.startsWith(WEB_INF_LIB_PREFIX)) {
        String jarName = name.replace(WEB_INF_LIB_PREFIX, '')
        if(jarName.startsWith('/') || jarName.startsWith('\\')) {
          jarName = jarName.substring(1)
        }
        if(jarName.isEmpty()) {
          return null
        }
        File jarFile = webInfJarMap.get(jarName)
        if(jarFile != null) {
          return jarFile
        }
      }
      return null
    }
    return file
  }

  private static class FileResource extends Resource {
    /**
     * Associated file object.
     */
    protected File file;

    public FileResource(File file) {
      this.file = file;
    }

    /**
     * Content accessor.
     *
     * @return InputStream
     */
    @Override
    public InputStream streamContent()
            throws IOException {
      if (binaryContent == null) {
        FileInputStream fis = new FileInputStream(file);
        inputStream = fis;
        return fis;
      }
      return super.streamContent();
    }
  }
}
