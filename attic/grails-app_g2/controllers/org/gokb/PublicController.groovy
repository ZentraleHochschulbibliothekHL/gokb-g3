package org.gokb

import grails.converters.*
import org.springframework.security.acls.model.NotFoundException
import org.springframework.security.access.annotation.Secured;
import org.gokb.cred.*
import grails.plugin.gson.converters.GSON
import org.springframework.web.multipart.MultipartHttpServletRequest
import com.k_int.ConcurrencyManagerService;
import com.k_int.ConcurrencyManagerService.Job
import java.security.MessageDigest
import grails.converters.JSON

import org.hibernate.ScrollMode
import org.hibernate.ScrollableResults
import org.hibernate.type.*
import org.hibernate.Hibernate



class PublicController {

  def genericOIDService
  def springSecurityService
  def concurrencyManagerService
  def TSVIngestionService
  def ESWrapperService
  def ESSearchService
  def grailsApplication
  def sessionFactory

  public static String TIPPS_QRY = 'select tipp from TitleInstancePackagePlatform as tipp, Combo as c where c.fromComponent.id=? and c.toComponent=tipp  and c.type.value = ? order by tipp.id';



  def packageContent() {
    log.debug("packageContent::${params}")
    def result = [:]
    if ( params.id ) {
      def pkg_id_components = params.id.split(':');
      def pkg_id = pkg_id_components[1]
      result.pkgData = Package.executeQuery('select p.id, p.name from Package as p where p.id=?',[Long.parseLong(pkg_id)])
      result.pkgId = result.pkgData[0][0]
      result.pkgName = result.pkgData[0][1]
      log.debug("Tipp qry name: ${result.pkgName}");
      result.tipps = TitleInstancePackagePlatform.executeQuery(TIPPS_QRY,[result.pkgId, 'Package.Tipps'],[offset:0,max:10])
      log.debug("Tipp qry done ${result.tipps?.size()}");
    }
    result
  }


  def index() {
    def result = [:]
    params.max = 30

    params.rectype = "Package" // Tells ESSearchService what to look for

    if(params.q == "")  params.remove('q');
    params.isPublic="Yes"
    if(params.lastUpdated){
      params.lastModified ="[${params.lastUpdated} TO 2100]"
    }
    if (!params.sort){
      params.sort="sortname"
      params.order = "asc"
    }
    if(params.search.equals("yes")){
      //when searching make sure results start from first page
      params.offset = 0
      params.search = null
    }
    if(params.filter == "current")
      params.tempFQ = " -pkg_scope:\"Master File\" -\"open access\" ";

    result =  ESSearchService.search(params)
    result.transforms = grailsApplication.config.packageTransforms

    result
  }


  // @Transactional(readOnly = true)
  def kbart() {

    def pkg = genericOIDService.resolveOID(params.id)

    def sdf = new java.text.SimpleDateFormat('yyyy-MM-dd')
    def export_date = sdf.format(new java.util.Date());

    def filename = "GOKb Export : ${pkg.name} : ${export_date}.tsv"

    try {
      response.setContentType('text/tab-separated-values');
      response.setHeader("Content-disposition", "attachment; filename=\"${filename}\"")
      response.contentType = "text/tab-separated-values" // "text/tsv"

      def out = response.outputStream
      out.withWriter { writer ->

        def sanitize = { it ? "${it}".trim() : "" }

          // As per spec header at top of file / section
          // II: Need to add in preceding_publication_title_id
          writer.write('publication_title\t'+
                       'print_identifier\t'+
                       'online_identifier\t'+
                       'date_first_issue_online\t'+
                       'num_first_vol_online\t'+
                       'num_first_issue_online\t'+
                       'date_last_issue_online\t'+
                       'num_last_vol_online\t'+
                       'num_last_issue_online\t'+
                       'title_url\t'+
                       'first_author\t'+
                       'title_id\t'+
                       'embargo_info\t'+
                       'coverage_depth\t'+
                       'coverage_notes\t'+
                       'publisher_name\t'+
                       'preceding_publication_title_id\t'+
                       'date_monograph_published_print\t'+
                       'date_monograph_published_online\t'+
                       'monograph_volume\t'+
                       'monograph_edition\t'+
                       'first_editor\t'+
                       'parent_publication_title_id\t'+
                       'publication_type\t'+
                       'access_type\n');

          // scroll(ScrollMode.FORWARD_ONLY)
          def session = sessionFactory.getCurrentSession()
          def query = session.createQuery("select tipp.id from TitleInstancePackagePlatform as tipp, Combo as c where c.fromComponent.id=:p and c.toComponent=tipp  and tipp.status.value <> 'Deleted' and c.type.value = 'Package.Tipps' order by tipp.id")
          query.setReadOnly(true)
          query.setParameter('p',pkg.getId(), Hibernate.LONG)


          ScrollableResults tipps = query.scroll(ScrollMode.FORWARD_ONLY)

          while (tipps.next()) {
            def tipp_id = tipps.get(0);

              TitleInstancePackagePlatform.withNewSession {
                def tipp = TitleInstancePackagePlatform.get(tipp_id)
                writer.write(
                            sanitize( tipp.title.name ) + '\t' +
                            sanitize( tipp.title.getIdentifierValue('ISSN') ) + '\t' +
                            sanitize( tipp.title.getIdentifierValue('eISSN') ) + '\t' +
                            sanitize( tipp.startDate ) + '\t' +
                            sanitize( tipp.startVolume ) + '\t' +
                            sanitize( tipp.startIssue ) + '\t' +
                            sanitize( tipp.endDate ) + '\t' +
                            sanitize( tipp.endVolume ) + '\t' +
                            sanitize( tipp.endIssue ) + '\t' +
                            sanitize( tipp.url ) + '\t' +
                            '\t'+  // First Author
                            sanitize( tipp.title.getId() ) + '\t' +
                            sanitize( tipp.embargo ) + '\t' +
                            sanitize( tipp.coverageDepth ) + '\t' +
                            sanitize( tipp.coverageNote ) + '\t' +
                            sanitize( tipp.title.getCurrentPublisher()?.name ) + '\t' +
                            sanitize( tipp.title.getPrecedingTitleId() ) + '\t' +
                            '\t' +  // date_monograph_published_print
                            '\t' +  // date_monograph_published_online
                            '\t' +  // monograph_volume
                            '\t' +  // monograph_edition
                            '\t' +  // first_editor
                            '\t' +  // parent_publication_title_id
                            sanitize( tipp.title?.medium?.value ) + '\t' +  // publication_type
                            sanitize( tipp.paymentType?.value ) +  // access_type
                            '\n');
                tipp.discard();
              }
          }

          tipps.close()

          writer.flush();
          writer.close();
        }
      out.close()
    }
    catch ( Exception e ) {
      log.error("Problem with export",e);
    }
  }

  def packageTSVExport() {


    def sdf = new java.text.SimpleDateFormat('yyyy-MM-dd')
    def export_date = sdf.format(new java.util.Date());


    def pkg = genericOIDService.resolveOID(params.id)

    if ( pkg == null )
      return;

    def filename = "GoKBPackage-${params.id}.tsv";

    try {
      response.setContentType('text/tab-separated-values');
      response.setHeader("Content-disposition", "attachment; filename=\"${filename}\"")
      response.contentType = "text/tab-separated-values" // "text/tsv"

      def out = response.outputStream
      out.withWriter { writer ->

        def sanitize = { it ? "${it}".trim() : "" }




          // As per spec header at top of file / section
          writer.write("GOKb Export : ${pkg.provider?.name} : ${pkg.name} : ${export_date}\n");

          writer.write('TIPP ID	TIPP URL	Title ID	Title	TIPP Status	[TI] Publisher	[TI] Imprint	[TI] Published From	[TI] Published to	[TI] Medium	[TI] OA Status	'+
                     '[TI] Continuing series	[TI] ISSN	[TI] EISSN	Package	Package ID	Package URL	Platform	'+
                     'Platform URL	Platform ID	Reference	Edit Status	Access Start Date	Access End Date	Coverage Start Date	'+
                     'Coverage Start Volume	Coverage Start Issue	Coverage End Date	Coverage End Volume	Coverage End Issue	'+
                     'Embargo	Coverage note	Host Platform URL	Format	Payment Type\n');

          def session = sessionFactory.getCurrentSession()
          def query = session.createQuery("select tipp.id from TitleInstancePackagePlatform as tipp, Combo as c where c.fromComponent.id=:p and c.toComponent=tipp  and tipp.status.value <> 'Deleted' and c.type.value = 'Package.Tipps' order by tipp.id")
          query.setReadOnly(true)
          query.setParameter('p',pkg.getId(), Hibernate.LONG)

          ScrollableResults tipps = query.scroll(ScrollMode.FORWARD_ONLY)

          while (tipps.next()) {

            def tipp_id = tipps.get(0);

            TitleInstancePackagePlatform tipp = TitleInstancePackagePlatform.get(tipp_id)

            writer.write( sanitize( tipp.getId() ) + '\t' + sanitize( tipp.url ) + '\t' + sanitize( tipp.title.getId() ) + '\t' + sanitize( tipp.title.name ) + '\t' +
                          sanitize( tipp.status.value ) + '\t' + sanitize( tipp.title.getCurrentPublisher()?.name ) + '\t' + sanitize( tipp.title.imprint?.name ) + '\t' + sanitize( tipp.title.publishedFrom ) + '\t' +
                          sanitize( tipp.title.publishedTo ) + '\t' + sanitize( tipp.title.medium?.value ) + '\t' + sanitize( tipp.title.oa?.status ) + '\t' +
                          sanitize( tipp.title.continuingSeries?.value ) + '\t' +
                          sanitize( tipp.title.getIdentifierValue('ISSN') ) + '\t' +
                          sanitize( tipp.title.getIdentifierValue('eISSN') ) + '\t' +
                          sanitize( pkg.name ) + '\t' + sanitize( pkg.getId() ) + '\t' + '\t' + sanitize( tipp.hostPlatform.name ) + '\t' +
                          sanitize( tipp.hostPlatform.primaryUrl ) + '\t' + sanitize( tipp.hostPlatform.getId() ) + '\t\t' + sanitize( tipp.status?.value ) + '\t' + sanitize( tipp.accessStartDate )  + '\t' +
                          sanitize( tipp.accessEndDate ) + '\t' + sanitize( tipp.startDate ) + '\t' + sanitize( tipp.startVolume ) + '\t' + sanitize( tipp.startIssue ) + '\t' + sanitize( tipp.endDate ) + '\t' +
                          sanitize( tipp.endVolume ) + '\t' + sanitize( tipp.endIssue ) + '\t' + sanitize( tipp.embargo ) + '\t' + sanitize( tipp.coverageNote ) + '\t' + sanitize( tipp.hostPlatform.primaryUrl ) + '\t' +
                          sanitize( tipp.format?.value ) + '\t' + sanitize( tipp.paymentType?.value ) +
                          '\n');
            tipp.discard();
          }
        }

        writer.flush();
        writer.close();
      out.close()
    }
    catch ( Exception e ) {
      log.error("Problem with export",e);
    }
  }
}
