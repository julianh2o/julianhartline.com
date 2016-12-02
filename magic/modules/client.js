import Util from "./util";

$(document).ready(function() {
    console.log("loaded index js",Util.guid());

    var canvas = new fabric.Canvas("c");
    canvas.selection = false;

    $.get("/state",function(data) {
        canvas.loadFromJSON(data, function () {
            canvas.renderAll();
        });
    });

    $(window).resize(function() {
        canvas.setWidth(window.innerWidth);
        canvas.setHeight(window.innerHeight);
    });
    $(window).resize();

    var panning = false;
    canvas.on('mouse:up', function (e) {
        if (!panning) {
            var obj = objectAt(canvas,e.e.clientX,e.e.clientY,e.target);
            if (obj) {
                if (obj.kind == "card") {
                    var group = createCollection(canvas,[obj,e.target],"stack");
                    console.log("created group",group.id);
                } else {
                    console.log("adding to group",obj.id);
                    addToCollection(canvas,obj,e.target);
                }
            }
        }
        panning = false;
    });

    canvas.on('mouse:beforedrag', function (e) {
        var target = canvas.findTarget(e);
        if (e.shiftKey && target.kind == "collection") {
            var group = target;
            var found = null;
            _.each(group.getObjects(),function(o) {
                if (containsInGroupPoint(o,new fabric.Point(e.clientX,e.clientY))) {
                    found = o;
                }
            });
            if (found) {
                console.log("removing from group",found.id,found.card.name);
                var left = found.left;
                var top = found.top;
                group.removeWithUpdate(found);
                canvas.add(found);
                if (group._objects.length == 1) {
                    var o = group._objects[0];
                    group._restoreObjectsState();
                    canvas.remove(group);
                    canvas.add(o);
                } else {
                    addToCollection(canvas,group,null);
                }
                found.bringToFront();
                found.setCoords();
            }
        }
    });

    canvas.on('mouse:down', function (e) {
        console.log("mouse down called",e.target == null ? null : e.target.kind);
        if (e.target == null) panning = true;
    });
    var laste = null;
    canvas.on('mouse:move', function (e) {
        laste = e;
        if (panning && e && e.e) {
            var delta = new fabric.Point(e.e.movementX, e.e.movementY);
            canvas.relativePan(delta);
        }
    });

    var last = null;
    var zoom = 1;
    $(window).on("mousewheel",function(e) {
        if (last && new Date().getTime() - last < 100) return;
        //e.originalEvent.deltaX,e.originalEvent.deltaY
        if (e.originalEvent.deltaY > 0) {
            zoom = zoom * 1.2;
        } else if (e.originalEvent.deltaY < 0) {
            zoom = zoom / 1.2;
        }
        canvas.setZoom(zoom)
        last = new Date().getTime();
    });

    $(window).on("keydown",function(e) {
        //console.log(e.keyCode);
    });
});
