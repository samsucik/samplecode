<?php
/**
 * Template Name: Alumni Sort
 *
 * This is not a template, it is just the back end for the Alumni system.
 *
 * @package WordPress
 * @subpackage Twenty_Thirteen_Child
 * @since Twenty Thirteen 1.0
 */
		global $lang;

		$path = "/wp/wp-content/themes/twentythirteenchild/";

		// social captions in both languages
		$social_en = array("email" => "e-mail", 
						"website" => "website", 
						"facebook" => "Facebook", 
						"twitter" => "Twitter", 
						"linkedin" => "LinkedIn", 
						"skype" => "Skype", 
						"gplus" => "Google+");
		$social_sk = array("email" => "e-mail", 
						"website" => "webstránku", 
						"facebook" => "Facebook", 
						"twitter" => "Twitter", 
						"linkedin" => "LinkedIn", 
						"skype" => "Skype", 
						"gplus" => "Google+");

		// read and assign request parameters
		$sort_par = $_REQUEST["s"];
		$uni = $_REQUEST["u"];
		$year = $_REQUEST["y"];
		$area = $_REQUEST["a"];
		$country = $_REQUEST["c"];

		// choose correct social captions
		$social = ($lang == 'sk' ? $social_sk : $social_en); 


		/******************************************************************
			Construct the SQL query for retrieving the desired alumni
		******************************************************************/
		$sql = "SELECT id, name, surname, ";
		
		// ask for contact details
		foreach ($social as $key => $value) $sql .= $key . ", ";
		
		// ask for other details
		$sql .= "photo, year, uni, country, area, degree, uni_id,
					 country_id, area_id FROM alumni_full WHERE name IS NOT NULL ";
		
		// if a particular university/year/subject area/country specified, 
		// include the conditions in the query
		$sql .= ($uni != "0" ? "AND (uni_id=" . $uni . ") " : "") 
		. ($year != "0" ? "AND (year=" . $year . ") " : "")
		. ($area != "0" ? "AND (area_id=" . $area . ") " : "")
		. ($country != "0" ? "AND (country_id=" . $country . ") " : "");

		// sort by desired parameter (received via Request)
		$sql .= " GROUP BY id" . ($sort_par == "0" ? "" : ", " . $sort_par);

		$sql .= " ORDER BY";
		// if sorting parameter specified, order by sorting parameter (year/uni/country/area), 
		// putting null values at the end, ordering in ascending order except when the sorting
		// parameter is "year", in which case sort in descending order (fresh alumni first)
		if ($sort_par != "0"){
			$sql .= " CASE 
	    		WHEN " . $sort_par . " IS NULL THEN 2
	    		ELSE 1
	    		END";
			if ($sort_par == "year"){
				$sql .= " DESC, " . $sort_par . " DESC,";
			}else{
				$sql .= " ASC, " . $sort_par . " ASC,";
			}
		}
		// sort alphabetically by name in any case
		$sql .= " name ASC";

		/**********************************************************************
			Retrieve and process the results
		**********************************************************************/
		// initialise the DB connection and fetch query results
		global $wpdb;
		$loop_table = $wpdb->get_results($sql);
		$oldHeading = "-";

		// if results are not empty, let's process them
		if (count($loop_table) > 0) 
		{
			// for each alumnus
			foreach ($loop_table as $k => $loop_row) {
				// heading if first of its kind (e.g. first from a particular university)
				// only applies if the sorting parameter has been specified (not "0")
				if ($sort_par != "0" and $loop_row->$sort_par != $oldHeading){
					$oldHeading = $loop_row->$sort_par;
					echo "<div class='alumni-category-separator'>" . $loop_row->$sort_par . 
					"<a href='#content' class='to-top'>" . ($lang == 'sk' ? "Návrat hore" : "Back to top") . "</a></div>";
				}

				// output data of each matching alumnus
				// don't show university for each alumnus if they are sorted by university (show degree instead)
		        echo "<div class='alumni-box'>
				<div class='alumni-uni'>" . ($sort_par == "uni" ? $loop_row->degree : $loop_row->uni) . "</div>
				<div class='alumni-subject'>" . $loop_row->degree . "</div>";
				echo (is_null($loop_row->skype) ? "" : "<div style='display: none;width: 100%;height: auto;position: absolute;bottom: 35px;z-index: 1000;color: rgb(240,240,255);background-color: rgba(20,10,0,0.90);' id='skype-" . $loop_row->id . "'><b>Skype</b>: <i>" . $loop_row->skype . "</i></div>");				
				echo "<img src='https://ib.gjh.sk/wp-content/uploads/alumni/" . $loop_row->photo . "' class='alumni-box-image'>
				<div class='alumni-social-panel'>
					<span class='alumni-name-span'>";
					echo $loop_row->name . "</span><span class='alumni-name-span surname'> " . $loop_row->surname . "</span><div style='clear:both;'> </div>";
					
				// contact information
				$social_counter = 0;
				foreach ($social as $key => $value) {
					// if the person has the particular type of contact (e-mail, LinkedIn, Facebook...)
					// display maximum 4 means of contact (so that they fit in the UI nicely)
					if ($loop_row->$key != NULL && $social_counter < 4){
						$social_counter ++;
						switch ($key) {
							// the social buttons are mere <a> elements, but Skype and e-mail need to be
							// treated differently; e-mail opens the "compose" window, skype shows the nick
						 	case 'email':
						 		echo "<a class='alumni-email' id='" . $loop_row->id . "' name='" .
						 		$loop_row->name . " " . $loop_row->surname . "' onclick='jQuery.fn.writeAlumniEmail(this);'";
						 		break;
						 	case 'skype':
						 		echo "<a onclick='jQuery.fn.showSkype(this);' id='" . $loop_row->id . "' ";
						 		break;
						 	default:
						 		echo "<a href='" . $loop_row->$key . "' ";
						 		break;
						}
						// tooltip in the correct language
						echo "title='" . ($lang == 'sk' ? "Spoj sa s absolventom " . $loop_row->name . 
						" cez " : "Get in touch with " . $loop_row->name . 
						" via ") . $value . "' target='_blank'><img src='https://ib.gjh.sk/wp-content/uploads/alumni/"
						 . $key . ".svg'></a>";
					} 							
				}
				echo "
				</div>	
				</div>";			
			}
		// report if no results match the query or if something simply went wrong...
		} else {echo "<div class='alumni-category-separator'>" . ($lang == 'sk' ? "Žiadne výsledky, skúste zmeniť filtre." : 
			"No alumni found, try changing the filters.") . "</div>";}
?>