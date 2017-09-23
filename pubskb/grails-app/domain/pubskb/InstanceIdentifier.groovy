package pubskb

class InstanceIdentifier {

  Instance instance
  IdentifierNamespace namespace
  String value

  static constraints = {
    instance (nullable:false)
    namespace (nullable:false)
    value (nullable:false, blank:false, unique:['namespace'])
  }
}
