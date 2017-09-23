package packagekb


import grails.rest.*
import grails.converters.*

class PackageController {

	static responseFormats = ['json', 'xml']

  def packageOnboardingService
	
  def index() { 
    log.debug("packagekb.PackageController::index");
  }

  def admin() { 
    def result = [:]
    log.debug("packagekb.PackageController::admin ${params.vendor}");

    def file = request.getFile("package_file")
    if ( file ) {
      log.debug("Got package_file ${file} ${file?.contentType} ${file?.getOriginalFilename()} ${file?.class.name}");
      result.message='OK!'
      result.job_id = packageOnboardingService.submitOnboardingJob(file.getOriginalFilename(), file.inputStream, params.vendor, params.wait);
    }
    else {
      result.message="ERROR: package/${provider}/admin must be called with a package_file multipart parameter";
    }

    render result as JSON
  }
}
