import CardService from "./cardService";
import Util from "./util";
import FabricUtil from "./fabricUtil";

var fabric = require('fabric').fabric;

fabric.Card = fabric.util.createClass(fabric.Image, {
    type: "card",
    card: null,
    initialize: function (card,img,options) {
        this.callSuper("initialize", img, options);
        this.id = options && options.id || Util.guid();
        this.card = card;
        this.cardId = card.id;
        this.imageUrl = card.imageUrl;

        FabricUtil.hideRotationAndScalingControls(this);
    },
    toObject: function(propertiesToInclude) {
        return this.callSuper("toObject",["cardId","id","imageUrl"].concat(propertiesToInclude));
    },
});

//Static
fabric.Card.fromMetadata = function(card, callback, options) {
    fabric.util.loadImage(card.imageUrl, function(img) {
        callback && callback(new fabric.Card(card,img,options));
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
