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
grails.plugin.springsecurity.useBasicAuth = true
grails.plugin.springsecurity.basic.realmName = "GOKb API Authentication Required"
//Exclude normal controllers from basic auth filter. Just the JSON API is included
grails.plugin.springsecurity.filterChain.chainMap = [
  '/api/**': 'JOINED_FILTERS,-exceptionTranslationFilter',
  '/packages/deposit': 'JOINED_FILTERS,-exceptionTranslationFilter',
  '/admin/bulkLoadUsers': 'JOINED_FILTERS,-exceptionTranslationFilter',
  '/**': 'JOINED_FILTERS,-basicAuthenticationFilter,-basicExceptionTranslationFilter'
]

grails.plugin.springsecurity.controllerAnnotations.staticRules = [
  [ '/admin/**':                ['ROLE_SUPERUSER', 'IS_AUTHENTICATED_FULLY']],
  [ '/file/**':                 ['ROLE_SUPERUSER', 'IS_AUTHENTICATED_FULLY']],
  [ '/monitoring/**':           ['ROLE_SUPERUSER', 'IS_AUTHENTICATED_FULLY']],
  [ '/':                        ['permitAll']],
  [ '/index':                   ['permitAll']],
  [ '/index.gsp':               ['permitAll']],
  [ '/register/**':             ['permitAll']],
  [ '/packages/**':             ['permitAll']],
  [ '/public/**':               ['permitAll']],
  [ '/globalSearch/**':         ['ROLE_USER']],
  [ '/assets/**':               ['permitAll']],
  [ '/**/js/**':                ['permitAll']],
  [ '/**/css/**':               ['permitAll']],
  [ '/**/images/**':            ['permitAll']],
  [ '/**/favicon.ico':          ['permitAll']],
  [ '/api/esconfig':            ['permitAll']],
  [ '/api/capabilities':        ['permitAll']],
  [ '/api/downloadUpdate':      ['permitAll']],
  [ '/api/checkUpdate':         ['permitAll']],
  [ '/api/isUp':                ['permitAll']],
  [ '/api/userData':            ['permitAll']],
  [ '/user/**':                 ['ROLE_SUPERUSER', 'IS_AUTHENTICATED_FULLY']],
  [ '/role/**':                 ['ROLE_SUPERUSER', 'IS_AUTHENTICATED_FULLY']],
  [ '/securityInfo/**':         ['ROLE_SUPERUSER', 'IS_AUTHENTICATED_FULLY']],
  [ '/registrationCode/**':     ['ROLE_SUPERUSER', 'IS_AUTHENTICATED_FULLY']],
  [ '/aclClass/**':             ['ROLE_SUPERUSER', 'IS_AUTHENTICATED_FULLY']],
  [ '/aclSid/**':               ['ROLE_SUPERUSER', 'IS_AUTHENTICATED_FULLY']],
  [ '/aclObjectIdentity/**':    ['ROLE_SUPERUSER', 'IS_AUTHENTICATED_FULLY']],
  [ '/aclEntry/**':             ['ROLE_SUPERUSER', 'IS_AUTHENTICATED_FULLY']],
  [ '/oai':                     ['permitAll']],
  [ '/oai/**':                  ['permitAll']]
]

grails.plugin.springsecurity.filterChain.chainMap = [
        [pattern: '/assets/**',      filters: 'none'],
        [pattern: '/**/js/**',       filters: 'none'],
        [pattern: '/**/css/**',      filters: 'none'],
        [pattern: '/**/images/**',   filters: 'none'],
        [pattern: '/**/favicon.ico', filters: 'none'],
        [pattern: '/**',             filters: 'JOINED_FILTERS']
]


