package pubskb

class Work {

  String title

  static constraints = {
    title(nullable:true, blank:false);
  }

  static hasMany = [
    identifiers:WorkIdentifier
  ]

  static mappedBy = [
    identifiers:'work',
  ]

}
