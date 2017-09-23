package org.gokb.cred

import groovy.util.logging.Log4j

import org.gokb.refine.*

@Log4j
class PackageRevision {

  Package pkg
  Date ts
  String hash
  
  static mapping = {
  }

  static constraints = {
  }
}
