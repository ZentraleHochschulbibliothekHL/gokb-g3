package pubskb

class InstanceIdentifierErratum {

  Instance instance
  IdentifierNamespace namespace
  String value
  InstanceIdentifier useInstead

  static constraints = {
    instance (nullable:false, blank:false)
    namespace (nullable:false, blank:false)
    value (nullable:false, blank:false)
    useInstead (nullable:true, blank:false)
  }
}
