package pubskb

class RefdataCategory {

  String desc
  String label
  Set values

  static mapping = {
         id column:'rdc_id'
    version column:'rdc_version'
      label column:'rdc_label'
       desc column:'rdc_description', index:'rdc_description_idx'
     values sort:'value', order:'asc'

  }

  static hasMany = [
    values:RefdataValue
  ]

  static mappedBy = [
    values:'owner'
  ]

  static constraints = {
    label(nullable:true, blank:true)
  }

}
