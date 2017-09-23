package packagekb

class Work {

  String title

  static hasMany = [
    identifiers:IdentifierOccurrence
  ]

  static mappedBy = [
    identifiers:'work'
  ]

    static constraints = {
    }
}
