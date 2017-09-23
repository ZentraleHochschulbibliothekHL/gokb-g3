import java.text.SimpleDateFormat
import java.util.concurrent.Executors

possible_date_formats = [
    new SimpleDateFormat('yyyy/MM/dd'),
    new SimpleDateFormat('dd/MM/yyyy'),
    new SimpleDateFormat('dd/MM/yy'),
    new SimpleDateFormat('yyyy/MM'),
    new SimpleDateFormat('yyyy')
];

isxn_formatter = { issn_string ->
      def result = issn_string
      def trimmed = (issn_string?:'').trim()
      if ( trimmed.length() == 8 ) {
        result = issn_string.substring(0,4)+"-"+issn_string.substring(4,8)
      }
      return result;
    }


identifiers = [
  "class_ones" : [
    "issn",
    "eissn",
    "doi",
    "isbn",
    "issnl"
  ],

  // Class ones that need to be cross-checked. If an Identifier supplied as an ISSN,
  // is found against a title but as an eISSN we still treat this as a match
  "cross_checks" : [
    ["issn", "eissn"],
    ["issn", "issnl"],
    ["eissn", "issn"],
    ["eissn", "issnl"],
    ["issnl", "issn"],
    ["issnl", "eissn"]
  ],

  formatters : [
    'issn' : isxn_formatter,
    'eissn' : isxn_formatter
  ]
]

project_dir = new java.io.File(org.grails.io.support.GrailsResourceUtils.GRAILS_APP_DIR + "/../project-files/").getCanonicalPath() + "/"


// Added by the Spring Security Core plugin:
grails.plugin.springsecurity.userLookup.userDomainClassName = 'org.gokb.cred.User'
grails.plugin.springsecurity.userLookup.authorityJoinClassName = 'org.gokb.cred.UserRole'
grails.plugin.springsecurity.authority.className = 'org.gokb.cred.Role'

//Enable Basic Auth Filter
// grails.plugin.springsecurity.useBasicAuth = true
// grails.plugin.springsecurity.basic.realmName = "GOKb API Authentication Required"

grails.plugin.springsecurity.controllerAnnotations.staticRules = [
        [pattern: '/',               access: ['permitAll']],
        // Remember that these patterns use the controller name and not the UrlMapping name,
        // This pattern allows okapi to use the tenant interface without authentication
        [pattern: '/okapi/**',       access: ['permitAll']],
        [pattern: '/home/index',     access: ['permitAll']],
        [pattern: '/error',          access: ['permitAll']],
        [pattern: '/index',          access: ['permitAll']],
        [pattern: '/index.gsp',      access: ['permitAll']],
        [pattern: '/shutdown',       access: ['permitAll']],
        [pattern: '/assets/**',      access: ['permitAll']],
        [pattern: '/**/js/**',       access: ['permitAll']],
        [pattern: '/**/css/**',      access: ['permitAll']],
        [pattern: '/**/images/**',   access: ['permitAll']],
        [pattern: '/**/favicon.ico', access: ['permitAll']]
]

grails.plugin.springsecurity.filterChain.chainMap = [
        [pattern: '/assets/**',      filters: 'none'],
        [pattern: '/**/js/**',       filters: 'none'],
        [pattern: '/**/css/**',      filters: 'none'],
        [pattern: '/**/images/**',   filters: 'none'],
        [pattern: '/**/favicon.ico', filters: 'none'],
        [pattern: '/**',             filters: 'JOINED_FILTERS']
]

