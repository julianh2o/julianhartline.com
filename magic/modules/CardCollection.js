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

        this.positionCards(objects,objects[0].left, objects[0].top);

        this.callSuper("initialize", objects, options);

        _.each(objects,function(o) {
            o.remove();
        }.bind(this));
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
    positionCards : function(objects,ox,oy) {
        _.each(objects,function(o,index) {
            o.set({left: ox, top: oy + index*35});
            o.setCoords();
        });
    },
    _render : function(ctx) {
        this.callSuper("_render", ctx);
    },
    toObject: function(propertiesToInclude) {
        return this.callSuper("toObject",["id"].concat(propertiesToInclude));
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
