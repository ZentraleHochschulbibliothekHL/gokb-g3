package pubskb

import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification
import grails.test.hibernate.HibernateSpec

class InstanceControllerSpec extends HibernateSpec implements ControllerUnitTest<InstanceController> {

  static no_identifier_record = [ title: 'Decision and Control' ];

  static decision_and_control = [ title: 'Decision and Control', author: 'Beer, Stafford', identifiers: [ [ namespace:'isxn', value:'1234-5678' ] ] ]

  static cpij_1e = [ title: 'Concurrent Programming in Java', author: 'Lea, Doug', subtitle:'Design Principles and Patterns',
                     identifiers: [ [namespace:'isbn10', value:'0201695812'], 
                                    [namespace:'isbn13',value:'978-0201695816'] ],
                     work: [ title:'Concurrent Programming in Java', identifiers:[ [namespace:'uuid', value:'4fbfe341-8921-463d-a87e-a1a933eecf59'] ] ] ]

  static cpij_2e = [ title: 'Concurrent Programming in Java Second Edition', author: 'Lea, Doug', edition: '2e', subtitle:'Design Principles and Patterns',
                     identifiers: [ [namespace:'isbn10',value:'0201210090'] ],
                     work: [ title:'Concurrent Programming in Java', identifiers:[ [namespace:'uuid', value:'4fbfe341-8921-463d-a87e-a1a933eecf59'] ] ] ]

  static doWithSpring = {
        jsonSmartViewResolver(JsonViewResolver)
  }

  def setup() {
    Instance.saveAll(
      new Instance(title: 'Brain of the Firm'),
      new Instance(title: 'Platform for Change'),
      new Instance(title: 'Heart of Enterprise')
    )
  }

  def cleanup() {
  }

  void 'test the search action finds results'() {
    when: 'A query is executed that finds results'
      controller.search('rai', 10)

    then: 'The response is correct'
      response.json.size() == 1
      response.json[0].title == 'Brain of the Firm'
  }

  void 'test the resolve function - No identifier path'() {
    when: 'A Request containing no identifiers is passed to the resolve function'
      request.method='POST'
      request.json=no_identifier_record
      controller.resolve()

    then:
      response.status==400
  }

  void 'test the resolve function'() {
    when: 'An item not already known in the KB is resolved'
      request.method='POST'
      request.json=decision_and_control
      controller.resolve()

    then:
      response.json.title=='Decision and Control'
  }

  void 'test resolve with attached common work'() {

    // N.B. Manually trying to call resolve multiple times in the when block will mess things up -- don't even try to do it,
    // use the where block to specify the list of instance records
    when: 'multiple valid instances are resolved, with a common work'
      request.method='POST'
      request.json=cpij_1e
      controller.resolve()

    then: 'Only one work should be created that links the 2 records'
      response.json.title=='Concurrent Programming in Java'
  }
}
