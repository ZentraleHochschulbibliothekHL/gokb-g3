package org.gokb

import org.elasticsearch.client.Client
import org.elasticsearch.node.Node
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.transport.InetSocketTransportAddress

import static groovy.json.JsonOutput.*

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.QueryBuilders


class ESWrapperService {

  static transactional = false

  def grailsApplication
  TransportClient esclient = null;

  @javax.annotation.PostConstruct
  def init() {
    log.debug("init ES wrapper service");
    Settings settings = Settings.builder().put("cluster.name", "elasticsearch").build();
    esclient = new org.elasticsearch.transport.client.PreBuiltTransportClient(settings);
    esclient.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));
  }

  def index(index,typename,id,record) {
    def result=null;
    try {
      // Convert the record to JSON
      // def json_string = toJson( record )
      // log.debug("Sending to ${index} ${typename} \n${json_string}\n");
      def future = esclient.prepareIndex(index,typename,id).setSource(record)
      result=future.get()
    }
    catch ( Exception e ) {
      log.error("Error processing ${toJson(record)}",e);
    }
    result
  }

  def getClient() {
    return esclient
  }

  @javax.annotation.PreDestroy
  def destroy() {
    log.debug("Destroy");
  }

}
