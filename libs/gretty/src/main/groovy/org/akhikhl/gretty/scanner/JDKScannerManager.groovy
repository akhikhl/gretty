package org.akhikhl.gretty.scanner

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.akhikhl.gretty.ServerConfig
import org.akhikhl.gretty.WebAppConfig
import org.gradle.api.Project
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.file.*
import java.util.concurrent.TimeUnit

/**
 * @author timur.shakurov@dz.ru
 */
@CompileStatic(TypeCheckingMode.SKIP)
final class JDKScannerManager extends BaseScannerManager {
    private static final Logger logger = LoggerFactory.getLogger(JDKScannerManager)
    private static final WatchEvent.Kind[] KINDS = [StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY]
    private WatchService watcherService
    private Thread pollingThread

    Map<Path, Set<Path>> interestingFiles = [:]

    JDKScannerManager(Project project, ServerConfig sconfig, List<WebAppConfig> webapps, boolean managedClassReload) {
        super(project, sconfig, webapps, managedClassReload)
    }

    public static boolean available() {
        try {
            Class.forName("java.nio.file.WatchService")
            return true
        } catch (ClassNotFoundException cnfe) {
            return false
        }
    }

    @Override
    void startScanner() {
        List<File> scanDirs = getEffectiveScanDirs()
        configureFastReload(scanDirs)
        for(File f in scanDirs) {
            logger.info 'scanDir: {}'
        }
        configureScanner(scanDirs)
    }

    private void configureScanner(List<File> scanDirs) {
        watcherService = FileSystems.default.newWatchService()
        //
        List<File> dirs = []
        scanDirs.each {
            if(it.isFile()) {
                interestingFiles.get(it.parentFile.toPath(), [] as Set).add(it.toPath())
            } else {
                dirs.add(it)
            }
        }


        dirs.each {
            if(!it.exists()) {
                return
            }
            it.toPath().register(watcherService, KINDS)
            it.eachDirRecurse {
                it.toPath().register(watcherService, KINDS)
            }
        }
        //
        interestingFiles.keySet().each {
            it.register(watcherService, KINDS)
        }
        //
        pollingThread = Thread.start {
            try {
                watch: for (;;) {
                    WatchKey key = watcherService.take()
                    List<String> fileNames = []
                    fileNames.addAll(getFileNames(key))

                    if (!key.reset()) {
                        scanFilesChanged(fileNames)
                        logger.info "key is not valid anymore"
                        break
                    }
                    // Waiting for next batch
                    for(;;) {
                        WatchKey key2 = watcherService.poll(100, TimeUnit.MILLISECONDS)
                        if(!key2) {
                            break
                        }
                        fileNames.addAll(getFileNames(key2))
                        if(!key2.reset()) {
                            scanFilesChanged(fileNames)
                            logger.info 'key is not valid anymore'
                            break watch
                        }
                    }
                    scanFilesChanged(fileNames)
                }
            } catch (InterruptedException ex) {
                logger.info "Thread stopped"
                Thread.currentThread().interrupt()
            } catch (ClosedWatchServiceException cwex) {
                logger.info 'Watch service was closed'
            }
        }
        pollingThread.name = 'Polling thread'
    }

    private List<String> getFileNames(WatchKey key) {
        Path path = key.watchable() as Path
        List<WatchEvent<?>> events = key.pollEvents()
        return events.collect {
            def kind = it.kind()
            if (kind == StandardWatchEventKinds.OVERFLOW) {
                return null
            }
            Path filename = (Path) it.context()
            if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                if (filename.toFile().isDirectory()) {
                    filename.register(watcherService, KINDS)
                }
            }
            // We wanted to watch file events, not directory
            if (interestingFiles.containsKey(filename.parent) && !interestingFiles.get(filename.parent).contains(filename)) {
                return null
            }
            return filename
        }.findAll { it != null }.unique().collect { path.resolve(it).toAbsolutePath().toString() }
    }

    @Override
    void stopScanner() {
        // No need to interrupt thread as it will exit
        watcherService?.close()
        pollingThread = null
        super.stopScanner()
    }
}
