package pubskb


class RefdataValue {

  String value
  String icon
  String description
  String sortKey
  RefdataValue useInstead
  RefdataCategory owner

  static mapping = {
    id column:'rdv_id'
    version column:'rdv_version'
    owner column:'rdv_owner', index:'rdv_entry_idx'
    value column:'rdv_value', index:'rdv_entry_idx'
    description column:'rdv_desc'
    sortKey column:'rdv_sortkey'
    useInstead column:'rdv_use_instead'
    icon column:'rdv_icon'
  }

  static constraints = {
    icon(nullable:true, blank:true)
    description(nullable:true, blank:true, maxSize:64)
    useInstead(nullable:true, blank:false)
    sortKey(nullable:true, blank:false)
  }

  @Override
  public String toString() {
    return "${value}"
  }

  @Override
  public boolean equals (Object obj) {

    if (obj != null) {
      if ( obj instanceof RefdataValue ) {
        return obj.id == id
      }
    }

    return false
  }

}
