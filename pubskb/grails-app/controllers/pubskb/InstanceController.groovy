package pubskb

import groovy.transform.CompileStatic
import grails.rest.RestfulController

class InstanceController extends RestfulController {

  static responseFormats = ['json', 'xml']

  InstanceController() {
    super(Instance)
  }

  def search(String q, Integer max) { 

    log.debug("Search: ${q}");

    if (q) {
      def query = Instance.where { 
        title ==~ "%${q}%"
      }
      respond query.list(max: Math.min( max ?: 10, 100)) 
    }
    else {
      log.debug("No query - responding with empty list");
      respond([]) 
    }
  }

  /**
   * Resolve a json document describing an instance. If the document matches an instance already
   * in the KB, return the item, otherwise create a new item and return that.
   */
  def resolve() {
    log.debug("resolve ${request.JSON}");

    // Currently we require at least one identifier to process the record
    if ( request.JSON == null ) {
      render(status:400, text:'resolve method requires a JSON publication record, as described here: https://github.com/k-int/folio-svc-publications-kb')
    }
    else if ( ( request.JSON?.identifiers == null ) || ( request.JSON?.identifiers?.size() == 0 ) ) {
      render(status:400, text:'Record contains no identifiers, cannot contine')
    }
    else if ( request.JSON.identifiers instanceof Map ) {
      throw new RuntimeException("resolve requires an instance description with a list of identifiers. You have provided a map. Do you need to wrap your single identifier in a JSON array?");
    }
    else {
      // Attempt to locate based on the identifier and namespace. We OR together all the identifiers. If we match a single instance
      // we are in good shape (And might have discovered additional identifiers for that item). If we match multiple, we are in bad
      // shape and should error
      def instance_query_sw = new StringWriter();
      def instance_params = [:]
      def identifier_counter = 0;
      instance_query_sw.write('select i from Instance as i where exists ( select id from InstanceIdentifier as id where id.instance = i AND ( ');
      request.JSON.identifiers.each { source_rec_identifier ->

        if ( ( source_rec_identifier.get('namespace') == null ) || ( source_rec_identifier.get('value') == null ) ) {
          throw new RuntimeException("Record contains an identifier without namespace or value: ${source_rec_identifier}");
        }

        if ( identifier_counter > 0 ) {
          instance_query_sw.write ( ' OR ' );
        }

        instance_query_sw.write("( id.namespace.nsIdentifier = :ns_${identifier_counter} AND id.value = :id_${identifier_counter} ) ");
        instance_params['ns_'+identifier_counter] = source_rec_identifier.namespace
        instance_params['id_'+identifier_counter] = source_rec_identifier.value
        identifier_counter++;
      }
      instance_query_sw.write(') ) ');

      def instance_query = instance_query_sw.toString();

      log.debug("Look up instances by ID : ${instance_query} ${instance_params}");

      def matched_instances = Instance.executeQuery(instance_query,instance_params);

      switch ( matched_instances.size() ) {
        case 0:
          log.debug("Matched no existing instances -- create");
          respond(createInstance(request.JSON), status: 200)
          break;
        case 1:
          log.debug("Matched one existing instance -- enrich");
          respond matched_instances.get(0);
          break;
        default:
          log.debug("Matched mutiple existing instance -- error");
          respond([text:'Identifiers in record matched multiple items', status:400])
          break;
      }
    }
  }

  private def createInstance(instance_record) {

    // New instance, set scalar props
    def new_instance = new Instance(title: instance_record.title);

    if ( instance_record.work != null ) {
     new_instance.work = lookupOrCreateWork(instance_record.work)
    }

    new_instance.save(flush:true, failOnError:true)

    // Add in any identifiers
    instance_record.identifiers.each { id ->
      // For now, we create namespaces if we don't recognise them -- maybe this should be more controlled however
      def namespace = IdentifierNamespace.findByNsIdentifier(id.namespace) ?: new IdentifierNamespace(nsIdentifier:id.namespace).save(flush:true, failOnError:true);
      def new_instance_identifier = new InstanceIdentifier(namespace:namespace, value:id.value, instance:new_instance).save(flush:true, failOnError:true);
    }

    // Refresh
    new_instance.refresh();

    // Return
    new_instance
  }

  private def lookupOrCreateWork(work_record) {

      def result = null;

      if ( work_record.identifiers instanceof Map ) {
        throw new RuntimeException("lookupOrCreateWork requires a work with a list of identifiers. You have provided a map. Do you need to wrap your map in a list?");
      }

      def work_query_sw = new StringWriter();
      def work_params = [:]
      def identifier_counter = 0;
      work_query_sw.write('select w from Work as w where exists ( select id from WorkIdentifier as id where id.work = w AND ( ');
      work_record.identifiers.each { source_rec_identifier ->

        if ( ( source_rec_identifier.get('namespace') == null ) || ( source_rec_identifier.get('value') == null ) ) {
          throw new RuntimeException("Record contains an identifier without namespace or value: ${source_rec_identifier}");
        }

        if ( identifier_counter > 0 ) {
          work_query_sw.write ( ' OR ' );
        }

        work_query_sw.write("( id.namespace.nsIdentifier = :ns_${identifier_counter} AND id.value = :id_${identifier_counter} ) ");
        work_params['ns_'+identifier_counter] = source_rec_identifier.namespace
        work_params['id_'+identifier_counter] = source_rec_identifier.value
        identifier_counter++;
      }
      work_query_sw.write(') ) ');

      def work_query = work_query_sw.toString();

      log.debug("Look up works by ID : ${work_query} ${work_params}");

      def matched_works = Instance.executeQuery(work_query,work_params);

      switch ( matched_works.size() ) {
        case 0:
          log.debug("Matched no existing works -- create");
          result = createWork(work_record);
          break;
        case 1:
          log.debug("Matched one existing work -- enrich");
          result = matched_works.get(0);
          break;
        default:
          log.debug("Matched mutiple existing work -- error");
          throw new RuntimeException("Matched multiple work records. Cannot continue");
          break;
      }

    result
  }

  private def createWork(work_record) {
    def result = new Work(title:work_record.title);
    result.save(flush:true, failOnError:true);

    // Add in any identifiers
    work_record.identifiers.each { id ->
       // For now, we create namespaces if we don't recognise them -- maybe this should be more controlled however
       def namespace = IdentifierNamespace.findByNsIdentifier(id.namespace) ?: new IdentifierNamespace(nsIdentifier:id.namespace).save(flush:true, failOnError:true);
       def new_work_identifier = new WorkIdentifier(namespace:namespace, value:id.value, work:result).save(flush:true, failOnError:true);
    }

    // Refresh
    result.refresh();

    result
  }
}
