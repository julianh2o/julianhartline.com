(function() {
    $(document).ready(function() {
        //var $canvas = $("<canvas />");
        //$canvas.width("100");
        //$canvas.height("100");
        //$("body").append($canvas);
        //var ctx = $canvas[0].getContext("2d");
        //console.log("foo");
    	
    	var Panel = function() {
    	    this.init.apply(this, arguments);
    	}
    	
    	$.extend(Panel.prototype,{
    		init : function(x,y,w,h,color) {
	    		this.position = new THREE.Vector3(x,y);
	    		//this.rotateSpeed = new THREE.Vector3(0,0,0);
	    		//this.rotateDestination = new THREE.Vector3(0,0,0);
    			this.flipping = false;
    			this.flipCount = 0;
	    		this.flipTo = 0;
	    		this.mesh = this.createMesh(w,h,color);
	    		this.mesh.position = this.position;
    		},
    		
    		createMesh : function(w,h,color) {
    			return new THREE.Mesh(new THREE.PlaneGeometry(w,h), new THREE.MeshBasicMaterial( { color:color, side: THREE.DoubleSide } ) );	
    		},
    		
    		flip : function() {
    			this.flipCount ++;
    			this.flipping = true;
    			this.flipTo = this.mesh.rotation.x + Math.PI;
    			//console.log(this.mesh.rotation);
    			//rotateTo(this.mesh.rotation.inverse(),100);
    		},
    		
    		rotateTo : function(rot,ticks) {
    			var sub = rot.sub(this.mesh.rotation);
    			this.rotateSpeed = sub.divideScalar(ticks);
    			this.rotateDestination = rot;
    		},
    		
    		run : function() {
    			if (this.flipping) {
    				if (this.mesh.rotation.x < this.flipTo) {
    					this.mesh.rotation.x += .05;
    				} else {
    					this.mesh.rotation.x = this.flipTo;
    					this.flipping = false;
    				}
    			}
    			
//    			if (this.rotateSpeed && this.mesh.rotation.distanceTo(this.rotateDestination) > this.rotateSpeed.length()) {
//    				this.mesh.rotation.add(this.rotateSpeed);
//    			} else {
//    				this.mesh.rotateSpeed = null;
//    			}

            	//panels[i].mesh.rotation.x += Math.random()*.05;
    		},
    	});
    	
    	var panels = [];
        var camera, scene, renderer;
        var flipState = 0;
        var panelsX = 10;
        var panelsY = 5;
        var panelWidth = 5;
        var panelHeight = 5;
        var startX = -(panelsX/2)*panelWidth;
        var startY = -(panelsY/2)*panelHeight;
        var flipCount = 0;

        init();
        animate();

        function init() {

            camera = new THREE.PerspectiveCamera( 75, window.innerWidth / window.innerHeight, 1, 10000 );
            camera.position.z = 30;

            scene = new THREE.Scene();
            
        	var origin = new THREE.Vector2(0,0);
        	var fullDistance = origin.distanceTo(new THREE.Vector2(panelsX,panelsY));
            for (var x=0; x<panelsX; x++) {
	            for (var y=0; y<panelsY; y++) {
	            	var here = new THREE.Vector2(x,y);
	            	var colorValue = origin.distanceTo(here) / fullDistance;
					var color = new THREE.Color(0,0,colorValue);
					var panel = new Panel(startX+x*panelWidth,startY+y*panelHeight,panelWidth,panelHeight,color);
	            	panels.push(panel);
		            scene.add( panel.mesh );
	            }
            }

            renderer = new THREE.CanvasRenderer();
            renderer.setSize( window.innerWidth, window.innerHeight );

            document.body.appendChild( renderer.domElement );

        }

        function animate() {

            // note: three.js includes requestAnimationFrame shim
            requestAnimationFrame( animate );

            flipState += .01;
        	var origin = new THREE.Vector2(startX,startY);
        	var fullDistance = origin.distanceTo(new THREE.Vector2(panelsX*panelWidth/2,panelsY*panelWidth/2));
            for (var i=0; i<panels.length; i++) {
	        	var here = new THREE.Vector2(panels[i].mesh.position.x,panels[i].mesh.position.y);
	        	var ratio = origin.distanceTo(here) / fullDistance;
	        	if (ratio < flipState && flipCount == panels[i].flipCount) panels[i].flip();
            	panels[i].run();
            }
            
            if (flipState > 2) {
            	flipState = 0;
            	flipCount ++;
            }

            renderer.render( scene, camera );

        }
    });

})();
