package packagekb

class PartyAppearence {

  Party party
  Pkg pkg
  RefdataValue role

  static constraints = {
    pkg(nullable:true)
    role(nullable:true)
  }

}
