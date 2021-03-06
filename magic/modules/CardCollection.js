var fabric = require("fabric").fabric;
var _ = require("lodash");

import Util from "./util";
import Card from "./Card";
import FabricUtil from "./fabricUtil";

fabric.CardCollection = fabric.util.createClass(fabric.Group, {
    type: "cardCollection",
    initialize : function(objects, options) {
        options || ( options = { });

        this.id = options && options.id || Util.guid();

        FabricUtil.hideRotationAndScalingControls(this);

        var objects = this.dismantleExistingCollections(objects);

        options.collectionType = options.collectionType || "stack";
        options.top = options.top || objects[0].top;
        options.left = options.left || objects[0].left;
        options.originX = options.originX || "left";
        options.originY = options.originY || "top";

        this.collectionType = options.collectionType;

        _.each(objects,function(o) {
            o.remove();
        }.bind(this));
        this.callSuper("initialize", objects, options);

        this.repositionCards();
    },
    dismantleExistingCollections:function(objects) {
        return _.flatten(_.map(objects,function(o) {
            return o.type === "cardCollection" ? o.dismantle() : o;
        }.bind(this)));
    },
    dismantle:function() {
        var objects = this._objects;
        this._restoreObjectsState();
        var canvas = this.canvas;
        canvas.remove(this);
        _.each(objects,function(o) {
            canvas.add(o);
        }.bind(this));
        return objects;
    },
    removeCard:function(card) {
        if (this.getObjects().length == 2) return this.dismantle();

        this.removeWithUpdate(card);
        this.repositionCards();
        this.canvas.add(card);
    },
    insertCards:function(cardOrCards,index) {
        var cards = Array.isArray(cardOrCards) ? cardOrCards : [cardOrCards];
        if (index < 0) index = this.getObjects().length + index + 1;
        _.each(cards,function(card) {
            card.remove();
            this.insertWithoutUpdate(card,index);
        }.bind(this));
        this.repositionCards();
    },
    repositionCards : function() {
        var cardSize = new fabric.Point(this.getObjects()[0].width, this.getObjects()[0].height);
        var separation = 35;
        this.width = cardSize.x;
        this.height = cardSize.y + separation*(this.getObjects().length-1);
        _.each(this.getObjects(),function(o,index) {
            var p = new fabric.Point(0,index*separation);
            p.x = p.x - this.width / 2;
            p.y = p.y - this.height / 2;
            o.set({left: p.x, top: p.y});
            o.setCoords();
        }.bind(this));
        this.setCoords();
    },
    toObject: function(propertiesToInclude) {
        return this.callSuper("toObject",["id","collectionType","boosterSet"].concat(propertiesToInclude));
    },
    insertWithoutUpdate: function(object,index) { //copied mostly from fabric js group code
        if (object) {
            this._objects.splice(index, 0, object);
            object.group = this;
            object._set('canvas', this.canvas);
        }
    },
});

//Static
fabric.CardCollection.fromObject = function(object,callback) {
    fabric.util.enlivenObjects(object.objects, function (enlivenedObjects) {
        delete object.objects;
        callback && callback(new fabric.CardCollection(enlivenedObjects, object));
    });
}

fabric.CardCollection.async = true; //This makes object loading asynchronous, similar to image

export default fabric.CardCollection;
