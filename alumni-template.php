<?php
/**
 * Template Name: Alumni Page Template
 *
 * This is the template that displays and sorts alumni.
 *
 * @package WordPress
 * @subpackage Twenty_Thirteen_Child
 * @since Twenty Thirteen 1.0
 */
global $lang;
get_header(); ?>

	<div id="primary" class="content-area">
		<div id="content" class="site-content" role="main">
			<article class="hentry">
				<header class="entry-header">
					<?php if ( has_post_thumbnail() && ! post_password_required() ) : ?>
					<div class="entry-thumbnail">
						<?php the_post_thumbnail(); ?>
					</div>
					<?php endif; ?>
					<h1 class="entry-title"><?php the_title(); ?></h1>
				</header><!-- .entry-header -->

				<div class="entry-content">
				<p><?php echo ($lang == 'sk' ? "Ktoré predmety si vybrať na IB? Ako prebiehajú pohovory na Cambridge? Oplatí sa nastúpiť na univerzitu v Škótsku od druhého ročníka? Je lepšie študovať fyziku v Londýne alebo v Edinburgu? Spýtaj sa našich absolventov!"
				                             : "Which subjects to choose in IB? What is an interview at Cambridge like? Is it good to start from Year 2 at Scottish universities? Is Physics study better in London or in Edinburgh? Ask our alumni!"); ?>
				</p>
				<div id="dimmer">
				<!-- E-MAIL FORM -->
				<form id='alumni-email-form' method='POST' target="_top">
				<button id='close-email-form'>X</button>
				<h5><?php echo ($lang == 'sk' ? "Absolvent <span id='alumni-email-recipient'></span> dostane Váš e-mail." : 
					"Alumnus <span id='alumni-email-recipient'></span> will receive the e-mail."); ?></h5>
				<p>
				<input type="text" id='alumni-email-sender-name' required placeholder="<?php echo ($lang == 'sk' ? "Vaše meno" : "Your name") ?>"> *&nbsp;
				<input type="text" placeholder='<?php echo ($lang == 'sk' ? "Vaša e-mailová adresa" : "Your e-mail address") ?>' 
				id='alumni-email-sender' required pattern="^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$"> *</p>
				<?php

				$content = '';
				$editor_id = 'alumni-email-editor';
				$settings = array( 'media_buttons' => false, 
								   'quicktags' => false, 
								   'teeny' => true,
								   'editor_class' => 'alumni-email-textarea',
								   'textarea_rows' => 10);
				wp_editor( $content, $editor_id, $settings );
				?><br>
				<input type="submit" id="alumni-email-submit" name="" value="Send" style="position: relative; float: right">
				</form>
				<!-- END OF E-MAIL FORM -->
				</div>

					<div class="alumni-menu-panel">
					<table width="100%" class="alumni-menu alumni-order">
					  <tr width="100%">
					  	<td><b><?php echo ($lang == 'sk' ? "Zoradiť podľa:" : "Sort by:") ?></b></td>
						<td><button id="sortByYear" class="alumni-sort"><?php echo ($lang == 'sk' ? "Roka" : "Year") ?></button></td>
						<td><button id="sortByArea" class="alumni-sort"><?php echo ($lang == 'sk' ? "Predmetovej oblasti" : "Subject area") ?></button></td>
						<td><button id="sortByUniversity" class="alumni-sort"><?php echo ($lang == 'sk' ? "Univerzity" : "University") ?></button></td>
						<td><button id="sortByCountry" class="alumni-sort"><?php echo ($lang == 'sk' ? "Krajiny" : "Country") ?></button></td>
					  </tr>
					</table>
					<div width="100%" class="alumni-menu">
						<select class="alumni-sort" id="filterByYear">
							<option value="0"><?php echo ($lang == 'sk' ? "Rok" : "Year") ?></option>
						</select>&nbsp;<select class="alumni-sort" id="filterByArea">
							<option value="0"><?php echo ($lang == 'sk' ? "Predmetová oblasť" : "Sbject area") ?></option>
						</select>&nbsp;<select class="alumni-sort" id="filterByUniversity">
							<option value="0"><?php echo ($lang == 'sk' ? "Univerzita" : "University") ?></option>
						</select>&nbsp;<select class="alumni-sort" id="filterByCountry">
							<option value="0"><?php echo ($lang == 'sk' ? "Krajina" : "Country") ?></option>
						</select>&nbsp;<button id='resetFilters' style="margin:auto">X</button>
					</div>
					</div>
					<div id="requested_content"></div>
					<div style="clear:both">&nbsp;</div>
					<div style="clear:both"><i>Icons made by <a href="http://www.freepik.com" title="Freepik">Freepik</a>, 
					<a href="http://www.flaticon.com/authors/simpleicon" title="SimpleIcon">SimpleIcon</a>, 
					<a href="http://www.flaticon.com/authors/elegant-themes" title="Elegant Themes">Elegant Themes</a> and 
					<a href="http://www.flaticon.com/authors/picol" title="Picol">Picol</a> from 
					<a href="http://www.flaticon.com" title="Flaticon">www.flaticon.com</a> are licensed by 
					<a href="http://creativecommons.org/licenses/by/3.0/" title="Creative Commons BY 3.0">CC BY 3.0</a>
					</i></div>
				</div>
			</article>
		</div><!-- #content -->
	</div><!-- #primary -->

<?php get_sidebar(); ?>
<?php get_footer(); ?>
<script type="text/javascript">
	jQuery(document).ready(function($) {

	    $.fn.scaleAlumni = function() {
	    	var contentWidth = parseInt($(".entry-content").css("width"));
      		var margin = 7;
			var width = 145+2*margin;
			var no = Math.floor(contentWidth/width);
			var rest = contentWidth - no*width;
			// prevent devision by 0
			if (no < 1) no = 1; 
			var toAdd = Math.floor(margin + rest/(2.0*no)) - 1 + "px";
			$(".alumni-box").css("marginRight",toAdd);
			$(".alumni-box").css("marginLeft",toAdd);
			$(".alumni-box").css("marginBottom",toAdd);
   		}; 

   		$.fn.makeDropdown = function(param) {
			var data = {
		        action: 'alumni_dropdown',
		        s: param,
		        y: $("#filterByYear").val(), 
		        a: $("#filterByArea").val(),
		        u: $("#filterByUniversity").val(),
		        c: $("#filterByCountry").val(),
		    	_ajax_nonce: '<?php echo wp_create_nonce( 'my_ajax_nonce' ); ?>'
		    };
		    var ajaxurl = '<?php echo admin_url('admin-ajax.php'); ?>';
		    jQuery.post(ajaxurl, data, function(response) {
		        var resp = response;
		        $("#filterBy" + param).html(resp);
		    });			
		};

		$.fn.sortAlumni = function (str) {
		    if (str.length == 0) { 
		        $("#requested_content").html("Sorry, something went wrong. Try to refresh the page.");
		    } else {
		    	var arr = ["Year", "University", "Area", "Country"];
			    var data = {
			        action: 'alumni_fetch',
			        s: str,
			        y: $("#filterByYear").val(), 
		        	a: $("#filterByArea").val(),
		        	u: $("#filterByUniversity").val(),
		        	c: $("#filterByCountry").val(),
			    	_ajax_nonce: '<?php echo wp_create_nonce( 'my_ajax_nonce' ); ?>'
			    };
			    var ajaxurl = '<?php echo admin_url('admin-ajax.php'); ?>';
			    jQuery.post(ajaxurl, data, function(response) {
			        var resp = response;
			        $("#requested_content").html(resp);
			        $.fn.scaleAlumni();
			    });
				for (param of arr) $.fn.makeDropdown(param);
		    }
		}

		$("button.alumni-sort").click(function(){
			switch ($(this).attr('id')){
				case 'sortByYear':
					$.fn.sortAlumni('year');
					break;
				case 'sortByUniversity':
					$.fn.sortAlumni('uni');
					break;
				case 'sortByArea':
					$.fn.sortAlumni('area');
					break;
				case 'sortByCountry':
					$.fn.sortAlumni('country');
					break;
				default:
					$.fn.sortAlumni('year');
			}
		});

		$("select.alumni-sort").change(function(){
			$.fn.sortAlumni("0");			
		});

		$( window ).resize(function() {
			$.fn.scaleAlumni();
		});

		var arr = ["Year", "Area", "University", "Country"];
		for (param of arr) $.fn.makeDropdown(param);
	    $.fn.sortAlumni("year");
		$(window).load(function(){
			$.fn.scaleAlumni();
		});
		
		$.fn.writeAlumniEmail = function(obj){
			var id = parseInt(obj.id);
			var name = obj.name;
			$('#alumni-email-recipient').html(name);
			$('#alumni-email-submit').attr("name", id);
			$('#dimmer').css("display", "block");
			$('#alumni-email-form').css("display", "block");
		};

		$('#alumni-email-form').submit(function(){
			var from = $('#alumni-email-sender').val();
			var to = parseInt($('#alumni-email-submit').attr("name"));
			var fromName = $('#alumni-email-sender-name').val();
			var msg = tinyMCE.activeEditor.getContent();
			if (msg == ""){alert("You cannot send an empty message.");} else{
				var data = {
			        action: 'alumni_send_email',
			        to: to,
			        from: from,
			        fromName: fromName, 
			        msg: msg,
			    	_ajax_nonce: '<?php echo wp_create_nonce( 'my_ajax_nonce' ); ?>'
			    };
			    var ajaxurl = '<?php echo admin_url('admin-ajax.php'); ?>';
			    jQuery.post(ajaxurl, data, function(response) {
			        var resp = response;
			        alert(resp);
			    });
			    $('#alumni-email-form').css("display", "none");
			    $('#dimmer').css("display", "none");
			}			
			return false;
		});

		$('#close-email-form').click(function(){
			$('#dimmer').css("display", "none");
			$('#alumni-email-form').css("display", "none");	
		});

		$.fn.showSkype = function(elem){
			skype = $("#skype-" + elem.id);
			if (skype.css("display") == "block") {
				skype.css("display", "none"); 
			}else{
				skype.css("display", "block");
			}
		};

		$('#resetFilters').click(function(){
			var arr = ["Year", "Area", "University", "Country"];
			for (param of arr) $("#filterBy" + param).val(0);
			$.fn.sortAlumni("0");	
		});
	});
</script>