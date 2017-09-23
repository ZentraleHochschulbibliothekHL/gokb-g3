package pubskb


/**
 * An occurrence of a work.
 * The important item here is the discriminator. Discriminators are used to ensure a unique hash at instance creation time.
 */
class Instance {

  Work work
  String title
  String discriminator
  RefdataValue itemType // BKM-Books and monographs / SER - Series / etc

  static constraints = {
     work (nullable:true);
     title (nullable:false, blank:false)
     discriminator (nullable:true);
     itemType (nullable:true);
  }

  static hasMany = [
    identifiers:InstanceIdentifier,
    identifierErrors:InstanceIdentifierErratum
  ]

  static mappedBy = [
    identifiers:'instance',
    identifierErrors:'instance'
  ]
}
