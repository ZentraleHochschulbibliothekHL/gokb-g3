package gokb

class UrlMappings {

  static mappings = {
    "/$controller/$action?/$id?"{
      constraints {
        // apply constraints here
      }
    }

    "/_/tenant"(controller: 'okapi', action:'tenant')
    "/"(controller:'home',action:'index')
    "500"(view:'/error')
  }
}
