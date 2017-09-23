package packagekb

import grails.rest.*
import grails.converters.*

class JobStatusController {
	static responseFormats = ['json', 'xml']
	
  def index() { 
  }

  def lookup() {
    def result = [:]
    log.debug("JobStatusController::lookup(${params.id})");
    JobTracker jt = JobTracker.findByJobUuid(params.id)
    if ( jt ) {
      result.id = params.id
      result.status = jt.status
      result.result = jt.result
    }
    render result as JSON
  }
}
