package org.gokb

import grails.converters.*
import org.springframework.security.acls.model.NotFoundException
import org.springframework.security.access.annotation.Secured;

import grails.util.GrailsClassUtils
import org.gokb.cred.*

import grails.plugin.gson.converters.GSON

class SearchController {

  def genericOIDService
  def springSecurityService
  def classExaminationService
  def gokbAclService


  @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
  def index() {
    User user = springSecurityService.currentUser
    def start_time = System.currentTimeMillis();

    log.debug("Entering SearchController:index ${params}");

    def result = [:]

    result.max = params.max ? Integer.parseInt(params.max) : ( user.defaultPageSize ?: 10 );
    result.offset = params.offset ? Integer.parseInt(params.offset) : 0;

    if ( params.jumpToPage ) {
      result.offset = ( ( params.int('jumpToPage') - 1 ) * result.max )
    }
    
    result.hide = params.list("hide") ?: []

    if ( params.searchAction == 'save' ) {
      log.debug("Saving query... ${params.searchName}");
      def defn = [:] << params
      defn.remove('searchAction')

      try {
        log.debug("Saving..");
        def saved_search = new SavedSearch(name:params.searchName,owner:user,searchDescriptor:(defn as GSON).toString())
        saved_search.save(flush:true, failOnError:true)
        log.debug("Saved.. ${saved_search.id}");
      }
      catch ( Exception e ) {
        log.error("Problem",e);
      }
    }

    if ( params.det )
      result.det = Integer.parseInt(params.det)

    if ( params.qbe ) {
      if ( params.qbe.startsWith('g:') ) {
        // Global template, look in config
        def global_qbe_template_shortcode = params.qbe.substring(2,params.qbe.length());
        // log.debug("Looking up global template ${global_qbe_template_shortcode}");
        result.qbetemplate = grailsApplication.config.globalSearchTemplates[global_qbe_template_shortcode]
        // log.debug("Using template: ${result.qbetemplate}");
      }

      // Looked up a template from somewhere, see if we can execute a search
      if ( result.qbetemplate ) {
        log.debug("Execute query");
        doQuery(result.qbetemplate, params, result)
        log.debug("Query complete");
        result.lasthit = result.offset + result.max > result.reccount ? result.reccount : ( result.offset + result.max )
        
        // Add the page information.
        result.page_current = (result.offset / result.max) + 1
        result.page_total = (result.reccount / result.max).toInteger() + (result.reccount % result.max > 0 ? 1 : 0)
      }
      else {
        log.error("no template ${result?.qbetemplate}");
      }

      
      if ( result.det && result.recset ) {

        log.debug("Got details page");

        int recno = result.det - result.offset - 1

        if ( result.recset.size() >= recno) {

          if ( recno >= result.recset.size() ) {
            recno = result.recset.size() - 1;
            result.det = result.reccount;
          }
          else if ( recno < 0 ) {
            recno = 0;
            result.det = 0;
          }
  
          // log.debug("Trying to display record ${recno}");
  
          result.displayobj = result.recset.get(recno)
  
          if ( result.displayobj != null ) {
  
            result.displayobjclassname = result.displayobj.class.name
            result.displaytemplate = grailsApplication.config.globalDisplayTemplates[result.displayobjclassname]
            result.__oid = "${result.displayobjclassname}:${result.displayobj.id}"
      
            // Add any refdata property names for this class to the result.
            result.refdata_properties = classExaminationService.getRefdataPropertyNames(result.displayobjclassname)
            result.displayobjclassname_short = result.displayobj.class.simpleName
            result.isComponent = (result.displayobj instanceof KBComponent)
            
            result.acl = gokbAclService.readAclSilently(result.displayobj)
        
            if ( result.displaytemplate == null ) {
              log.error("Unable to locate display template for class ${result.displayobjclassname} (oid ${params.displayoid})");
            }
            else {
              // log.debug("Got display template ${result.displaytemplate} for rec ${result.det} - class is ${result.displayobjclassname}");
            }
          }
          else { 
            log.error("Result row for display was NULL");
          }
        }
        else {
          log.error("Record display request out of range");
        }
      }
    }

    def apiresponse = null
    if ( ( response.format == 'json' ) || ( response.format == 'xml' ) ) {
      apiresponse = [:]
      apiresponse.count = result.reccount
      apiresponse.max = result.max
      apiresponse.offset = result.offset
      apiresponse.records = []
      result.recset.each { r ->
        def response_record = [:]
        response_record.oid = "${r.class.name}:${r.id}"
        result.qbetemplate.qbeConfig.qbeResults.each { rh ->
          response_record[rh.heading] = groovy.util.Eval.x(r, 'x.' + rh.property)
        }
        apiresponse.records.add(response_record);
      }
    }

    result.withoutJump = params.clone()
    result.withoutJump.remove('jumpToPage');

    // log.debug("leaving SearchController::index...");
    log.debug("Search completed after ${System.currentTimeMillis() - start_time}");

    withFormat {
      html result
      json { render apiresponse as JSON }
      xml { render apiresponse as XML }
    }
  }

  def doQuery (qbetemplate, params, result) {
    def target_class = grailsApplication.getArtefact("Domain",qbetemplate.baseclass);
    com.k_int.HQLBuilder.build(grailsApplication, qbetemplate, params, result, target_class, genericOIDService)
  }

}
