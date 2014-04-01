(function() {
    $(document).ready(function() {
        //var $canvas = $("<canvas />");
        //$canvas.width("100");
        //$canvas.height("100");
        //$("body").append($canvas);
        //var ctx = $canvas[0].getContext("2d");
        //console.log("foo");

        var camera, scene, renderer;
        var geometry, material, mesh;

        init();
        animate();

        function init() {

            camera = new THREE.PerspectiveCamera( 75, window.innerWidth / window.innerHeight, 1, 10000 );
            camera.position.z = 30;

            scene = new THREE.Scene();

            //geometry = new THREE.CubeGeometry( 200 ,200 ,200 );
            //geometry = new THREE.PlaneGeometry( 100 ,100 );
            //material = new THREE.MeshBasicMaterial( { color: 0xff0000, wireframe: true, side: THREE.DoubleSide } );
            geometry = new THREE.TorusGeometry( 10, 3, 16, 100 );
            material = new THREE.MeshBasicMaterial( { color: 0xff0000, wireframe: true } );

            mesh = new THREE.Mesh( geometry, material );
            mesh.position.x = 10;
            mesh.position.y = 10;
            scene.add( mesh );

            renderer = new THREE.CanvasRenderer();
            renderer.setSize( window.innerWidth, window.innerHeight );

            document.body.appendChild( renderer.domElement );

        }

        function animate() {

            // note: three.js includes requestAnimationFrame shim
            requestAnimationFrame( animate );

            mesh.rotation.x += 0.01;
            //mesh.rotation.y += 0.05;
            //mesh.rotation.z += 0.05;

            renderer.render( scene, camera );

        }
    });

})();
