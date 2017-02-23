package gokb

import org.gokb.cred.*

class BootStrap {

  def init = { servletContext ->
    setUpRefdata();
    setUpAuth();
  }

  def destroy = {
  }

  def setUpRefdata() {
    KBComponent.withTransaction() {
      RefdataCategory.lookupOrCreate('YN', 'Yes').save()
      RefdataCategory.lookupOrCreate('YN', 'No').save()
    }
  }

  def setUpAuth() {
    log.debug("Set up auth");
    // Global System Roles
    KBComponent.withTransaction() {
      def contributorRole = Role.findByAuthority('ROLE_CONTRIBUTOR') ?: new Role(authority: 'ROLE_CONTRIBUTOR', roleType:'global').save(failOnError: true)
      def userRole = Role.findByAuthority('ROLE_USER') ?: new Role(authority: 'ROLE_USER', roleType:'global').save(failOnError: true)
      def editorRole = Role.findByAuthority('ROLE_EDITOR') ?: new Role(authority: 'ROLE_EDITOR', roleType:'global').save(failOnError: true)
      def adminRole = Role.findByAuthority('ROLE_ADMIN') ?: new Role(authority: 'ROLE_ADMIN', roleType:'global').save(failOnError: true)
      def apiRole = Role.findByAuthority('ROLE_API') ?: new Role(authority: 'ROLE_API', roleType:'global').save(failOnError: true)
      def suRole = Role.findByAuthority('ROLE_SUPERUSER') ?: new Role(authority: 'ROLE_SUPERUSER', roleType:'global').save(failOnError: true)
      def refineUserRole = Role.findByAuthority('ROLE_REFINEUSER') ?: new Role(authority: 'ROLE_REFINEUSER', roleType:'global').save(failOnError: true)
      def refineTesterRole = Role.findByAuthority('ROLE_REFINETESTER') ?: new Role(authority: 'ROLE_REFINETESTER', roleType:'global').save(failOnError: true)

      def no = RefdataCategory.lookupOrCreate('YN', 'No').save()
      def yes = RefdataCategory.lookupOrCreate('YN', 'Yes').save()

      log.debug("Create admin user...");
      def adminUser = User.findByUsername('admin')
      if ( ! adminUser ) {
        log.error("No admin user found, create")
        adminUser = new User(
            username: 'admin',
            password: 'admin',
            display: 'Admin',
            email: 'admin@localhost',
            send_alert_emails:no,
            showQuickView:yes,
            showInfoIcon:yes,
            enabled: true).save(failOnError: true)
      }

      def ingestAgent = User.findByUsername('ingestAgent')
      if ( ! ingestAgent ) {
        log.error("No ingestAgent user found, create")
        ingestAgent = new User(
            username: 'ingestAgent',
            password: 'ingestAgent',
            display: 'Ingest Agent',
            email: '',
            send_alert_emails:no,
            showQuickView:yes,
            showInfoIcon:yes,
            enabled: false).save(failOnError: true)
      }


      // Make sure admin user has all the system roles.
      [contributorRole,userRole,editorRole,adminRole,apiRole,suRole,refineUserRole,refineTesterRole].each { role ->
        if (!adminUser.authorities.contains(role)) {
          UserRole.create adminUser, role
        }
      }
    }
  }
}
