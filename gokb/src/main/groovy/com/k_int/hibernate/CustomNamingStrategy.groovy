package com.k_int.hibernate

import grails.util.Holders
import org.hibernate.cfg.ImprovedNamingStrategy

class CustomNamingStrategy extends ImprovedNamingStrategy {
    String classToTableName(String className){
        "gokb_${className.toLowerCase()}"
    }
}
