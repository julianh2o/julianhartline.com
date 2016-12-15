import Util from "./util";
import EventFactory from "./eventFactory";
import Table from "./table";
import FabricUtil from "./fabricUtil";
import Card from "./Card";
import CardCollection from "./CardCollection";

var fabric = require('fabric').fabric;
var fetch = require('node-fetch');

$(document).ready(function() {
    var table = new Table(new fabric.Canvas("c"));
    var host = window.document.location.host.replace(/:.*/, '');
    var ws = new WebSocket('ws://' + host + ':8080');
    window.table = table;

    $.get("/state").done(function(data) {
        table.loadFromJSON(data);
    });

    var canvas = table.canvas;

    $(window).resize(function() {
        canvas.setWidth(window.innerWidth);
        canvas.setHeight(window.innerHeight);
    });
    $(window).resize();

    var panning = false;
    canvas.on("mouse:up", function (e) {
        if (!panning) {
            var dropTarget = FabricUtil.objectAt(table.canvas,e.e.clientX,e.e.clientY,e.target);
            var dragged = e.target;
            if (dropTarget) {
                if (dropTarget.type === "card") {
                    emit(EventFactory.createCollection([dropTarget,dragged]),true);
                } else if (dropTarget.type === "cardCollection") {
                    emit(EventFactory.addToCollection(dropTarget,dragged),true);
                }
            }
        }
        panning = false;
    });
 
    ws.onmessage = function (e) {
        var e = JSON.parse(e.data);
        table.processEvent(e);
    };

    function emit(e,runLocal) {
        ws.send(JSON.stringify(e));
        if (runLocal) table.processEvent(e);
    }

    canvas.on("object:moving",function(e) {
        emit(EventFactory.updateObject(e.target),false);
    });

    canvas.on("mouse:beforedrag", function (e) {
        var target = canvas.findTarget(e);
        if (e.shiftKey && target.type == "cardCollection") {
            var collection = target;
            var card = FabricUtil.findCardInCollection(collection,e.clientX,e.clientY);
            if (!card) return;

            emit(EventFactory.removeFromCollection(card),true);
            card.bringToFront();
            card.setCoords();
        }
    });

    canvas.on("mouse:down", function (e) {
        if (e.target == null) panning = true;
    });
    var laste = null;
    canvas.on("mouse:move", function (e) {
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
        if (e.keyCode == 32) {
            var objects = table.canvas.getObjects();
            if (objects.length > 1) {
                emit(EventFactory.createCollection(objects),true);
            } else {
                emit(EventFactory.dismantleCollection(objects[0]),true);
            }
        }
    });
});
