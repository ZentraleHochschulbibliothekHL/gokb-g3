package packagekb


/**
 *  A line in a source file. Contains the standard KBART fields ready to try and resolve the
 *  item described into a title we know about. Non-standard fields are preserved in the additional_fields structure
 *
 */
class SourceFileRow {

  SourceFile partOf
  String notes                            // Free text
  String access_type                      // Constrained [F|P]
  String publication_type                 // Constrained [serial|monograph]

  // Journal only fields
  String date_first_issue_online
  String num_first_vol_online
  String num_first_issue_online
  String date_last_issue_online
  String num_last_vol_online
  String num_last_issue_online

  // Journals and books
  String print_identifier
  String online_identifier
  String publication_title
  String title_url
  String title_id

  String date_monograph_published_print
  String date_monograph_published_online
  String monograph_volume
  String monograph_edition
  String first_editor
  String first_author

  String parent_publication_title_id
  String preceding_publication_title_id
  
  String embargo_info
  String coverage_depth
  String publisher_name

  static constraints = {
    notes(nullable:true)
    access_type(nullable:true)
    preceding_publication_title_id(nullable:true)
    publication_type(nullable:true)
    date_first_issue_online(nullable:true)
    num_first_vol_online(nullable:true)
    num_first_issue_online(nullable:true)
    date_last_issue_online(nullable:true)
    num_last_vol_online(nullable:true)
    num_last_issue_online(nullable:true)
    print_identifier(nullable:true)
    online_identifier(nullable:true)
    publication_title(nullable:true)
    title_url(nullable:true)
    title_id(nullable:true)
    date_monograph_published_print(nullable:true)
    date_monograph_published_online(nullable:true)
    monograph_volume(nullable:true)
    monograph_edition(nullable:true)
    first_editor(nullable:true)
    first_author(nullable:true)
    parent_publication_title_id(nullable:true)
    embargo_info(nullable:true)
    coverage_depth(nullable:true)
    publisher_name(nullable:true)
  }
}
