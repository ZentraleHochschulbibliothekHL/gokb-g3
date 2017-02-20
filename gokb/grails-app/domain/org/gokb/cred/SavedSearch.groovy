package org.gokb.cred

import javax.persistence.Transient
import grails.converters.JSON

class SavedSearch {

  String name
  User owner
  String searchDescriptor

  static constraints = {
    name blank: false, nullable:false
    owner blank: false, nullable: false
    searchDescriptor blank: false, nullable:false
  }

  static mapping = {
    id column: 'ss_id'
    name column: 'ss_name'
    owner column: 'ss_owner_fk'
    searchDescriptor column: 'ss_search_descriptor', type:'text'
  }

  @Transient
  def toParams() {
    def jsonObject = JSON.parse(searchDescriptor) // (Map) gson.fromJson(searchDescriptor, Object.class);
    return jsonObject
  }
}
