package packagekb

class JobTracker {

  String jobUuid
  String status
  Long startTime
  Long endTime
  String result

    static constraints = {
      jobUuid(nullable:false)
      status(nullable:false)
      startTime(nullable:false)
      endTime(nullable:true)
      result(nullable:true)
    }

  static mapping = {
    result type:'text'
  }
}
