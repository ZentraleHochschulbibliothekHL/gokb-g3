package packagekb

import grails.gorm.transactions.Transactional
import org.apache.commons.io.FileUtils
import au.com.bytecode.opencsv.CSVReader
import static grails.async.Promises.*
import grails.converters.JSON

/**
 *
 * Package files pass through a specific workflow.
 *   1. initial validation - Make sure the file is structurally sound, and has the mandatory fields
 *   2. Transform the file on disk into Source / SourceFile / SourceFileRow / CustomFieldValue / CustomFieldDefn structures in the DB
 *   3. Attempt to low level validate all the rows. Make sure we know the titles, validate title/id parings etc. This may well be an iterative process. 
 *   4. Merge the package file into our package database proper
 *   5. Delete the temporary structures
 */
@Transactional
class PackageOnboardingService {

  public String submitOnboardingJob(filename, input_stream, vendor, w) {
    log.debug("submitOnboardingJob(${filename},...,${vendor},${w})");
    def job_id = java.util.UUID.randomUUID().toString()

    JobTracker jt = new JobTracker(jobUuid:job_id, status:'RUNNING', startTime:System.currentTimeMillis()).save(flush:true, failOnError:true);

    // Copy file.inputStream to temporary file
    def target_file = File.createTempFile("package-upload-${job_id}", ".tmp");
    FileUtils.copyInputStreamToFile(input_stream, target_file);


    // We could do task { DomainObj.withNewSession { ... instead if we wanted
    def p1 = task {
      def r = null;
      JobTracker.withNewSession {
        r=runPackageLoad(job_id, target_file)
      }
      r
    }

    if ( ( p1 != null ) && ( w?.equals('true') ) ) {
      log.debug("Waiting for promise to complete");
      waitAll(p1)
      log.debug("Complete");
    }

    return job_id
  }

  private Map runPackageLoad(job_id, package_file) {
    log.debug("runPackageLoad(${job_id},...)");
    def result = [:]
    log.debug("Copied upload file to temp file, now validate");


    log.debug("runPackageLoad completing -- update tracker");
    def result_as_str = (result as JSON).toString()
    JobTracker.executeUpdate('update JobTracker set status=:comp, result=:res where jobUuid = :id',[id:job_id, res:result_as_str, comp:'COMPLETE']);
    log.debug("runPackageLoad complete");

    return result
  }

  public Map validateSourceFile(File file) {
    log.debug("validateSourceFile");
    def result = [:]

    CSVReader r = new CSVReader( new InputStreamReader(file.inputStream, java.nio.charset.Charset.forName('UTF-8') ) )
    def first = true
    def broken = false
    String[] nl=null;
    while ((nl = r.readNext()) != null && !broken) {
      if ( first ) {
        first = false; // Skip header
      }
      else {
      }
      log.debug("Line ${nl}");
    }


    result.status = true
    result.errors = []
    result
  }


  public void initialLoad() {
  }
}
