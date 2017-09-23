package pubskb

class WorkIdentifier {

  Work work
  IdentifierNamespace namespace
  String value

  static constraints = {
    work (nullable:false)
    namespace (nullable:false)
    value (nullable:false, blank:false, unique:['namespace'])
  }
}
