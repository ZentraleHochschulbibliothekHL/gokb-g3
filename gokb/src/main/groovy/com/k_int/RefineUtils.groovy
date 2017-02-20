package com.k_int

import groovy.util.logging.Log4j

import org.grails.web.json.*

@Log4j
class RefineUtils {

  public static String getRowValue(datarow, col_positions, colname, recon_data = null) {

    String result = null
    if ( col_positions[colname] != null ) {
      result = jsonv(datarow.cells[col_positions[colname]],recon_data)
    }
    result
  }

  public static def jsonv(v, recon_data = null) {
    def result = null

    // Thoroughly check for nulls.
    if (v && !(v.equals(null) || JSONObject.NULL.equals(v) ) ) {

      // First check if we have recon data then we should look that up instead.
      if (recon_data && v.r != null && !JSONObject.NULL.equals(v.r)) {

        def recon = recon_data[v.r]

        def ids = recon?.get('identifierSpace')

        // Now we should check the identifierSpace.
        if ( ids && "gokb".equalsIgnoreCase(ids)) {

          // Let's grab the value.
          result = recon.'m'?.'id'

          if (result) {
            result = "gokb::{${result}}"
            return result
          }
        }
      }

      if (v.v != null && !JSONObject.NULL.equals(v.v)) {
        result = "${v.v}"
      }
    }
    result
  }

}
