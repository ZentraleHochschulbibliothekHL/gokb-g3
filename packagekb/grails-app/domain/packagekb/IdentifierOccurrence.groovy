package packagekb

class IdentifierOccurrence {

  TitleInstance titleInstance
  Work work
  Identifier identifier

  static constraints = {
    titleInstance(nullable:true)
    work(nullable:true)
  }

  static hasMany = [
  ]

  static mappedBy = [
  ]
}
