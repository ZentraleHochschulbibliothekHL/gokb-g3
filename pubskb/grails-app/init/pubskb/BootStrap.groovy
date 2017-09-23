package pubskb

class BootStrap {

  def init = { servletContext ->
    log.debug("Bootstrap");
  }

  def destroy = {
  }
}
