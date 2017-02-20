package org.gokb

import grails.transaction.Transactional

import grails.commons.GrailsApplication

import com.k_int.RefineUtils
import com.k_int.TextUtils


@Transactional
class RefineService {
  static scope = "singleton"
  
  private static final String EXTENSION_PREFIX = "gokb-release-"
  private static final String EXTENSION_SUFFIX = ".zip"
  private static final String NAMING_REGEX = "\\Q${EXTENSION_PREFIX}\\E${TextUtils.VERSION_REGEX}"
  private static final String STABLE_RELEASE_NAMING_REGEX = "\\Q${EXTENSION_PREFIX}\\E${TextUtils.NONE_ALPHA_VERSION_REGEX}"
  private static final String FILENAME_REGEX = "${NAMING_REGEX}\\Q${EXTENSION_SUFFIX}\\E"
  private static final String STABLE_FILENAME_REGEX = "${STABLE_RELEASE_NAMING_REGEX}\\Q${EXTENSION_SUFFIX}\\E"

  GrailsApplication grailsApplication

  /**
   * File filter to only get refine downloads.
   */
  private static FilenameFilter filter = new FilenameFilter() {
    public boolean accept(File dir, String name) {
      return name ==~ FILENAME_REGEX
    }
  };

  private static FilenameFilter stableFilter = new FilenameFilter() {
    public boolean accept(File dir, String name) {
      return name ==~ STABLE_FILENAME_REGEX
    }
  };

  private static class VersionedStringComparitor implements Comparator<String> {
    public int compare(String s1, String s2) {

      // Now we have the versions we can compare them.
      return TextUtils.versionCompare(s1.replaceFirst(FILENAME_REGEX, "\$1\$4\$6"), s2.replaceFirst(FILENAME_REGEX, "\$1\$4\$6"));
    }
  }

  private static final VersionedStringComparitor comp = new VersionedStringComparitor()

  private String refineFolderSingleton
  public String getRefineFolder() {
    if (!refineFolderSingleton) refineFolderSingleton = grailsApplication.mainContext.getResource('refine').file.absolutePath
    refineFolderSingleton
  }

  private String getLatestCurrentLocalExtension (boolean tester = false) {

    // Open the webapp_dir
    File folder = new File(refineFolder)

    // Ensure that the folder is a directory.
    if (folder.isDirectory()) {

      // Get a list of all GOKb extension zips.
      String[] extensions = folder.list(tester ? filter : stableFilter)
      if(extensions){
        // Sort the results.
        Arrays.sort(extensions, comp)

        // Now we should have a sorted array. Take the last element.
        return extensions[extensions.length - 1].replaceFirst(FILENAME_REGEX, "\$1\$3")
      }
    }

    // Null if none could be found.
    null
  }

  /**
   * Need to check whether the currently available local tool is a "later" release
   * than the one currently in use by the user. 
   * 
   * @param current_version The user's current refine version.
   * @return
   */
  def checkUpdate (String current_version, boolean tester = false) {
    
    log.debug("Checking for update for ${current_version} ${(tester ? 'including' : 'excluding')} test versions.")
    
    // Update available
    boolean update = false;
    
    def data = [
      "latest-version" : null,
      "file-name"      : null
    ]
    
    // If this is the developer version of the tool then always report no update.
    if (current_version != 'development') {
      
      // Get the latest local version
      String current_local_version = getLatestCurrentLocalExtension(tester)
      
      log.debug("Found current version is ${current_local_version}")
      
      if (current_local_version) {
  
        data += [
          "latest-version" : current_local_version?.replaceFirst(FILENAME_REGEX, "\$1\$3"),
          "file-name"       : current_local_version
        ]
        
        // Handle the fact that previous refine versions will report incorrectly formatted version values here.
        if (current_version ==~ TextUtils.VERSION_REGEX) {
          
          log.debug ("Current version matches the regex ${TextUtils.VERSION_REGEX}")
          update = (comp.compare(current_local_version, current_version) > 0)
          log.debug ("Update has returned ${update}")
          
        } else {
          update = true;
        }
      }
    }
    
    data += ['update-available' : (update)]
    
    log.debug("returning ${data}")
    
    return data
  }
  
  File extensionDownloadFile (String version_required = null, boolean tester = false) {
    
    if (version_required == null) {
      version_required = getLatestCurrentLocalExtension(tester)
    }
    
    if (version_required ==~ TextUtils.VERSION_REGEX) {
      
      // Return file uri if the file exists.
      File f = new File (refineFolder, "${EXTENSION_PREFIX}${version_required}${EXTENSION_SUFFIX}")
      if (f.exists()) {
        return f
      }
    }
    
    null
  }
  
}
