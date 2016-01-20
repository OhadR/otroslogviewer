# OtrosLogViewer - WEB
Useful software for analysing applications logs and traces.

## Issues:
* [DONE] if client does not read from cache - data is not deleted from the cache. so it will blow. need to detect disconnected clients and remove their entries
* sleep between writes of chunks, to let clients read in between.
* [DONE] when a client asks for a file, stop other threads that were created for this client.
* UI - smaller font in the table.
* [DONE] visualizer to cache status - how many entries and threads - *backend*
* visualizer to cache status - how many entries and threads - *front end*
* [DONE] if cannot open file - close thread (easy)
* [DONE] show "WARN" in yellow (same as ERROR in red)
* [DONE] loading huge log files - show client only 50K recent lines, not all lines (could be 1.5M lines!, will take 10 minutes to load everything to UI)


## Features
  * Loading logs from remote servers using ftp, sftp, ssh, samba and others ([supported file systems](http://commons.apache.org/vfs/filesystems.html))
  * Tailing logs from local disk and sftp
  * Decompressing "gziped" logs on the fly
  * Parsing custom log patterns
  * Log events searching using regular expression
  * Log filters
  * Pluginable log filters
  * Log highlightings
  * Automatic log highlightings based on string match, regular expression or custom Java code
  * Pluginable log details formatters and colorizers (i.e. SOAP message)
  * Pluginable log highlightings
  * Pluginable log parsers
  * Listening on a socket
  * Adding notes to log event
  * Saving/loading log investigation (with added marks and notes)
  * Integration with [IntelliJ IDEA](https://github.com/otros-systems/otroslogviewer/wiki/JumpToCode) and [Eclipse](https://github.com/otros-systems/otroslogviewer/wiki/JumpToCode)

## Screenshots

Filter level WARNING or higher and highlighting:

![http://lh5.ggpht.com/_qGxhLPhk4wE/TTixTV-fyNI/AAAAAAAAA18/ILCMZ_siOIE/olv-2011-01-20.png](http://lh5.ggpht.com/_qGxhLPhk4wE/TTixTV-fyNI/AAAAAAAAA18/ILCMZ_siOIE/olv-2011-01-20.png)

[Click here to see more screenshots](https://github.com/otros-systems/otroslogviewer/wiki/Screenshots)

## Video
[Screen cap recording ](https://github.com/otros-systems/otroslogviewer/wiki/Wideo)

## Powered by
  * Otros Engine:
  * Apache commons: net, lang, vfs, httpclient, logging, collections, compress, io
  * log4j
  * (VFSJFileChooser)
  * jCIFS
  * jSch
  * EHCache
  * Spring
  * bootstrap table
  * jQuery
  

## Contact
If you have any questions please write to ohad.redlich at williamhill.com

