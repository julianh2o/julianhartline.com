import CardService from "./cardService";
import Util from "./util";
import FabricUtil from "./fabricUtil";

var fabric = require('fabric').fabric;
var _ = require("lodash");

fabric.Card = fabric.util.createClass(fabric.Image, {
    type: "card",
    card: null,
    initialize: function (card,img,options) {
        this.callSuper("initialize", img, options);
        this.id = options && options.id || Util.guid();
        this.card = card;
        this.cardId = card.id;
        this.imageUrl = card.imageUrl;
        this.flipped = false;
        this.cardElement = img;

        fabric.util.loadImage("http://localhost:3000/cardback.jpg", function(img) {
            this.backElement = img;
        }, this, true);

        FabricUtil.hideRotationAndScalingControls(this);
    },
    toObject: function(propertiesToInclude) {
        return this.callSuper("toObject",["cardId","id","imageUrl"].concat(propertiesToInclude));
    },
    setFlipped(flipped) {
        if (flipped === undefined) flipped = !this.flipped;

        this.flipped = flipped;
        this.setElement(flipped ? this.backElement : this.cardElement, function() {
            this.canvas.renderAll();
        }.bind(this), {width: this.width, height: this.height});
    }
});

//Static
fabric.Card.fromMetadata = function(card, callback, options) {
    var fabricId = card.fabricId;
    delete card.fabricId;
    fabric.util.loadImage(card.imageUrl, function(img) {
        callback && callback(new fabric.Card(card,img,_.extend({},options,{id: fabricId})));
    }, null, null);
}

fabric.Card.fromObject = function(object, callback) {
    if (object.imageUrl) {
        fabric.Card.fromMetadata({id:object.cardId,imageUrl:object.imageUrl},callback,object);
    } else {
        CardService.cardById(object.cardId).then(function(card) {
            fabric.Card.fromMetadata(card,function(card) {
                fabric.util.object.extend(card,object);
            });
        });
    }
}

fabric.Card.async = true; //This makes object loading asynchronous, similar to image


export default fabric.Card;
