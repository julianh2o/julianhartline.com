// ==UserScript==
// @name            Fetlife: Natural Mail Enhancement
// @namespace       http://www.julianhartline.com/
// @version         0.24
// @description     This plugin enhances Fetlife with additional functionality such as mass mailing
// @match           https://fetlife.com/*
// @require         http://ajax.googleapis.com/ajax/libs/jquery/1.6/jquery.min.js
// @copyright       2016+, Julian (julianh2o)
// ==/UserScript==

//Changelog
// 0.24
// Added a random delay between messages to try to fool Fetlife into thinking we're sending manually
// Added a status message that displays while messages are being sent and the final status when complete
// Made a change that hopefully fixes the bug where successfully sent messages were marked as errors
// When clicking send, non-selected friends are hidden to make it easier to see the progress

// 0.23
// Fixing bug that caused user list to fail to load

// 0.19
// Renamed to "Fetlife: Natural Mail Enhancement"

// 0.18
// Added export to clipboard feature on the recipients list in past messages

// 0.17
// Added import/export to clipboard feature on the mass mail page

// 0.16
// Fixed the formatting of the success message after sending a mass email
//
// 0.15
// Added profile picture to recipient selection screen, added profile links in the recipient list

(function() {
    var Fet = function() {};
    var localStorageKey = "fetlifeMassMailer";
	var DEFAULT = {"presets":[],"last":[]};
    $.extend(Fet.prototype,{
        init : function() {
            this.userId = FetLife.currentUser.id;
			this.downloadFriendlist();
            this.enhanceUi();
            this.loadPresets();
        },
        
        loadPresets : function() {
            this.data = DEFAULT;
            
            if (!window.localStorage) return;
            
            var stringData = localStorage.getItem(localStorageKey);
            if (!stringData) return;
            
            this.data = JSON.parse(stringData);
        },
        
        savePresets : function() {
            if (!window.localStorage) return;
            
            localStorage.setItem(localStorageKey,JSON.stringify(this.data)); 
        },
        
        clearPresets : function() {
            this.data = DEFAULT;
            this.savePresets();
        },
        
        enhanceUi : function() {
            var $inbox = $(".inbox.knockout-bound");
            var $clone = $inbox.clone();
            $inbox.before($clone);
            var $span = $("<span />").text("mass mail").css({
                "pointer-events":"none",
                "position": "absolute",
				"font-size": "10px",
				"color": "red",
				"z-index": "100",
				"top": "-14px",
				"left": "0px",
                "text-align":"center",
                "line-height":"24px"
            });
            $clone.removeClass(".inbox").addClass(".mass-mailer");
            $clone.prepend($span);
            $clone.find("a").click($.proxy(this.openMailer,this));

            $clone.hide();
            this.$massMailButton = $clone;
            
            function hereDoc(f) {
  				return f.toString().
      			replace(/^[^\/]+\/\*!?/, '').
      			replace(/\*\/[^\/]+$/, '');
			}
            var css = hereDoc(function() {/*!
            	.recipient {
                	width: 200px;
                    display: inline-block;
                    border: 2px solid rgba(0,0,0,0);
                }
                .selected-recipient {
               		border-radius: 5px;
                	border: 2px solid #0f0;
                }
                .recipient-checkbox {
                	display: none;
                }
                .media {margin:5px;}
				.media, .bd {overflow:hidden; _overflow:visible; zoom:1;}
				.media .img {float:left; margin-right: 10px;width:50px;}
				.media .img img{display:block;}
				.media .imgExt{float:right; margin-left: 10px;}
			*/});
            $("body").append("<style type='text/css'>"+css+"</style>");
            
            var csv = "";
            $("a[href='http://bit.ly/1n396JN']").parent().prev().find("a").each(function() {
                var href = $(this).attr("href");
                var id = href.substring(href.lastIndexOf("/")+1);
                csv += id +",";
            });
            $("a[href='http://bit.ly/1n396JN']").parent().before($("<a href='#'>Export Userlist</a>").click(function() {
                prompt("Copy and paste",csv);
                return false;
            }));
        },
        
        makeMailForm : function() {
        	var $content = $("<div />").addClass("container");
            var $recipients = $("<div />");
            for (i=0; i<this.friends.length; i++) {
                var person = this.friends[i];
                var $media = $("<div class='media recipient'></div>");
                var $icon = $("<img class='img' src='"+person.iconUrl+"'>")
                var $bd = $("<div class='bd'></div>");
                var $label = $("<label class='recipientlabel'>"+person.username+"</label>");
                var $checkbox = $("<input class='recipient-checkbox' type='checkbox' value='"+person.id+"'>");
                $media.click(function() {
                    var $cb = $(this).find("input");
                    $cb.prop("checked",!$cb.is(":checked"));
                    $cb.change();
                });
                $checkbox.change(function() {
                    var checked = $(this).is(":checked");
                    $(this).parents(".media").toggleClass("selected-recipient",checked);
                });
                var $ulink = $("<a class='quiet xxs q un nowrap db'/>");
                $ulink.attr("href","https://fetlife.com/users/"+person.id);
                $ulink.attr("target","_blank");
                $ulink.text("profile");
                person.$el = $label;
                $ulink.click(function(e) {
                	e.stopPropagation(); 
                });
                $checkbox.data("user",person);
                
                //assemble dom
                $label.prepend($checkbox);
                $label.append($icon);
                $label.append(" ").append($ulink);
                
                $bd.append($label);
                
                $media.append($icon);
                $media.append($bd);
                
                $recipients.append($media);
            }
            //$recipients.find(".recipientlabel").css({
            //    "width":"200px",
            //    "display":"inline-block",
            //});
            
            $("<h4>Recipients:</h4>").appendTo($content).css("display","inline-block");
            
            var $presetsPanel = $("<div />").css("display","inline-block");
            var $presetsSelect = this.createPresetsDropdown();
            $presetsPanel.append($presetsSelect);
            $("<a href='#'>Save Selection</a>").appendTo($presetsPanel).click($.proxy(this.createPreset,this,$content));
            $("<span> | </span>").appendTo($presetsPanel);
            $("<a href='#'>Invert Selection</a>").appendTo($presetsPanel).click($.proxy(this.invertSelection,this,$content));
            $("<span> | </span>").appendTo($presetsPanel);
            $("<a href='#'>Export Selection</a>").appendTo($presetsPanel).click($.proxy(function() {
                var out = "";
                $content.find(".recipientlabel input").each(function() {
                    if ($(this).is(":checked")) out += $(this).val() + ","
                });
                prompt("Copy and paste",out);
            },this));
            $("<span> | </span>").appendTo($presetsPanel);
            $("<a href='#'>Import Selection</a>").appendTo($presetsPanel).click($.proxy(function() {
                var csv = prompt("Paste ids separated by commas:");
                var tokens = csv.split(",");
                $content.find(".recipientlabel input").prop("checked",false).change();
                for (var i=0; i<tokens.length; i++) {
                    if (tokens[i] == "") continue;
                    $content.find(".recipientlabel input[value="+tokens[i]+"]").prop("checked",true).change();
                }
            },this));
            
            $content.append($presetsPanel);
            
            $content.append($recipients);
            $content.append("<br/>");
            var $mailForm = $("<div class='mailform' />");
            $mailForm.append("<h4>Subject:</h4>");
            $mailForm.append($("<input type='text' class='subject'>").css("width","100%"));
            $mailForm.append("<br/>");
            $mailForm.append("<br/>");
            $mailForm.append("<h4>Message:</h4>");
            $mailForm.append($("<textarea class='message'>").css("width","100%"));
            $mailForm.append("<br/>");
            $mailForm.append("<br/>");
            $mailForm.append("<label><input type='checkbox' class='includelist' checked>Include recipient list with message</label>");
            $mailForm.append("<br/>");
            $mailForm.append("<label><input type='checkbox' class='includead' checked>Include the link to download this plugin (spread the wealth!)</label>");
            $mailForm.append("<div><button class='sendbutton'>Send</button></div>");
            $content.append($mailForm);
            return $content;
        },
        
        createPresetsDropdown : function() {
            var $presetsSelect = $("<select class='presetsDropdown' />");
            $("<option />").appendTo($presetsSelect).val("").text("select a preset").prop("disabled",true).prop("selected",true);
            for (var i=0; i<this.data.presets.length; i++) {
                $("<option />").appendTo($presetsSelect).val(i).text(this.data.presets[i].name);
            }
            $presetsSelect.change($.proxy(this.presetSelected,this));
            return $presetsSelect;
        },
        
        presetSelected : function(e) {
            var val = $(e.target).val();
            this.$dialog.find(".recipientlabel input").prop("checked",false);
            var ids = this.data.presets[val].ids;
            for (var i=0; i<ids.length; i++) {
                this.$dialog.find("input[value="+ids[i]+"]").prop("checked",true).change();
            }
        },
        
        invertSelection : function($el,e) {
            e.preventDefault();
            $el.find(".recipientlabel input").each(function() {
                $(this).prop("checked",!$(this).is(":checked"));
            });
        },
        
        createPreset : function($el,e) {
            e.preventDefault();
			var presetName = prompt("Enter a name for this group");
			if (!presetName) return;
            
            var idlist = [];
            $el.find(".recipientlabel input:checked").each(function() {
                idlist.push($(this).data("user").id);
            });
            this.data.presets.push({
                name: presetName,
                ids: idlist,
            });
                        
            $el.find(".presetsDropdown").replaceWith(this.createPresetsDropdown());
            
            this.savePresets();
        },
        
        makeMailDialog : function() {
            var $dialog = $("<div />").addClass("clearfix");
            this.$dialog = $dialog;

            $dialog.append(this.makeMailForm());
            
            //$("#header_v2").next().empty().append($dialog);
            return $dialog;
        },
        
        openMailer : function(e) {
			e.preventDefault();
            e.stopPropagation();
            
            var $dialog = this.makeMailDialog();
            $dialog.find(".sendbutton").click($.proxy(this.sendMessage,this,$dialog));
            
            $(".container").replaceWith($dialog);
        },
        
        getRecipients : function($el) {
            var recipients = [];
            $.each($el.find(".recipientlabel input"),function() {
                if ($(this).is(":checked")) {
                    recipients.push($(this).data("user"));
                }
            });
            return recipients;
        },
        
        sendMessage : function($dialog) {
            var subject = $dialog.find(".subject").val();
            var message = $dialog.find(".message").val();
            var includeRecipients = $dialog.find(".includelist").is(":checked");
            var includeAd = $dialog.find(".includead").is(":checked");
            var recipients = this.getRecipients($dialog);
            
            if (message.length == 0 || subject.length == 0) {
                alert("Neither message nor subject may be blank!");
                return;
            }
            
            $dialog.find(".mailform").hide();
            $dialog.find("input").css("opacity","0");
            $dialog.find("label").css("color","#666");
            
            var stringRecipients = "";
            var plainStringRecipients = "";
            for (var i=0; i<recipients.length;i++) {
                recipients[i].$el.css("color","white");
                stringRecipients += ", ["+recipients[i].username+"][https://fetlife.com"+recipients[i].profileUrl+"]";
                plainStringRecipients += ", "+recipients[i].username;
            }
            stringRecipients = stringRecipients.substring(2);
            plainStringRecipients = plainStringRecipients.substring(2);
            
            if (includeRecipients) {
                message += "\n\nThis message was sent to the following recipients: "+stringRecipients;
            }

            if (includeAd) {
                message += "\n\n#### This message was sent with [http://bit.ly/2jSx3FS][Fetlife: Natural Mail Enhancement] by [https://fetlife.com/users/1436573][@julianh2o]."
            }
            
            var successes = 0;
            var failures = 0;
            function success(user) {
                successes++;
                user.$el.css("color","#0f4");
            }
                    
            function fail(user) {
                failures++;
                user.$el.css("color","#f00");
            }
            
            var delays = [];
            for (var i=0; i<recipients.length; i++) delays.push(Math.floor(5000+Math.random()*8000));
            
            var $status = $("<div />").css({
                "position": "fixed",
                "bottom": "0px",
                "height": "30px",
                "background-color":"#ff9900",
                "font-size":"30px",
                "line-height":"30px",
                "text-align":"center",
                "width":"100%",
                "padding":"20px",
                "color":"black",
                "font-weight":"bold",
            });

            $(".recipient").not(".selected-recipient").hide();

            $(document.body).append($status);

            var lastSend = new Date().getTime();
            var lastIndex = 0;
            var secondTick = setInterval(function() {
                var remainingDelay = 0;
                for (var l=lastIndex; l<recipients.length-1; l++) remainingDelay += delays[l];
                var currentTime = new Date().getTime();
                var sinceLastSend = currentTime - lastSend;
                remainingDelay -= sinceLastSend;

                remainingDelay = Math.floor(remainingDelay / 1000);
                $status.text("Sending message "+(lastIndex+1)+" of "+recipients.length+" ("+remainingDelay+" seconds remain)");
            },1000);

            function sendNext(i) {
                var user = recipients[i];
                console.log("sending.. ",i,user);
                lastSend = new Date().getTime();
                lastIndex = i;

                $.get("/conversations/new?with="+user.id).done($.proxy(function(user,data) {
                    var token = $(data).filter("#authenticity_token").text();
                    
                    var data = {
                        "subject":subject,
                        "body":message,
                        "authenticity_token":token,
                        "commit":"Start+Conversation",
                        "with":user.id,
                    };

                    setTimeout(function() {
                        $.post("/conversations",data)
                            .done($.proxy(success,this,user))
                            .fail($.proxy(fail,this,user))
                            .always(function() {
                                if (i+1 >= recipients.length) {
                                    clearInterval(secondTick);
                                    if (failures == 0) {
                                        $status.css("background","green");
                                        $status.text("All messages send successfully!");
                                    } else {
                                        $status.css("background","red");
                                        $status.text(failures+" messages failed to send!");
                                    }
                                    return;
                                }
                                sendNext(i+1);
                            });
                    },(delays[i]));
                                        
                },this,user)).fail($.proxy(fail,this,user));
            }

            sendNext(0);
            
            
            var $successMessage = $("<div>Your message is being sent to the following recipients: "+plainStringRecipients+"<br/>Green indicates success, red indicates failure.</div>");
            $dialog.find(".container").append($successMessage);
        },
        
        //https://fetlife.com/users/{uid}/friends
        downloadFriendlist : function() {
            this.friends = [];
            this.currentFriendPage = 1;
            this.friendDataHandler(null);
    	},
        
        friendsLoaded : function() {
            this.$massMailButton.show();
        },
        
        friendDataHandler : function(data) {
            if (data) {
                var people = this.processFriends($(data).find(".fl-member-card"));
                this.friends = this.friends.concat(people);
                if (!people.length) {
                	this.friendsLoaded();
                    return;
                }
            }
            $.get("https://fetlife.com/users/"+this.userId+"/friends",{page:this.currentFriendPage}).done($.proxy(this.friendDataHandler,this));
            this.currentFriendPage++;
        	
        },
        
        infoPattern: /([0-9]*)([^ ]*) (.*)/,
        processFriends : function($els) {
            var pattern = this.infoPattern;
            var friends = [];
            $els.each(function() {
                var person = {};
                person.iconUrl = $(this).find(".fl-avatar__img").attr("src");
                person.profileUrl = $(this).find(".fl-avatar__link").attr("href");
                person.username = $(this).find(".fl-member-card__user").text();
                person.id = person.profileUrl.substring(person.profileUrl.lastIndexOf("/")+1);
                
                var info = $(this).find(".fl-member-card__info").text();
                var match = pattern.exec(info);
                person.age = match[1];
                person.gender = match[2];
                person.identity = match[3];
                
                person.location = $(this).find(".fl-member-card__location").text();
                friends.push(person);
            });
            return friends;
        }
    });
    
    $(document).ready(function() {
        if (typeof FetLife == "undefined") return;
        new Fet().init();
    });
})();






