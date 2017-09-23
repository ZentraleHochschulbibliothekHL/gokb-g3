package packagekb

class TitleInstance {

  static constraints = {
  }

  static hasMany = [
    identifiers:IdentifierOccurrence
  ]

  static mappedBy = [
    identifiers:'titleInstance'
  ]
}
