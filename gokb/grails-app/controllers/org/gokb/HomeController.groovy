package org.gokb

import org.springframework.security.access.annotation.Secured;
import grails.util.GrailsNameUtils

import org.gokb.cred.*
import org.hibernate.SessionFactory;
import org.hibernate.transform.AliasToEntityMapResultTransformer
import grails.core.GrailsApplication


class HomeController {

  GrailsApplication grailsApplication
  def springSecurityService

  SessionFactory sessionFactory

  static stats_cache = null;
  static stats_timestamp = null;

  @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
  def login() {
  }

  def index () {
  }

}
