var fabric = require("fabric").fabric;
var _ = require("lodash");

import Util from "./util";
import FabricUtil from "./fabricUtil";
import CardService from "./CardService";
import constants from "./constants";

fabric.BoosterPack = fabric.util.createClass(fabric.Image, {
    type: "boosterPack",
    initialize : function(cards, options, callback) {
        options || ( options = { });

        this.id = options && options.id || Util.guid();
        this.cards = cards;

        options.width = constants.CARD_WIDTH;
        options.height = options.boosterImage.height * (options.width / options.boosterImage.width);

        options.top = options.y ? options.y - options.height / 2 : options.top;
        options.left = options.x ? options.x - options.width / 2 : options.left;
        delete options.x;
        delete options.y;

        this.top = options.top;
        this.left = options.left;

        console.log("spawn ots",options);

        FabricUtil.hideRotationAndScalingControls(this);

        this.boosterSet = options.boosterSet;

        this.callSuper("initialize", options.boosterImage, options);
    },
    open:function(callback) {
        var promiseList = _.map(this.cards,function(card,index) {
            return new Promise(function(resolve,reject) {
                fabric.Card.fromMetadata(card,resolve);
            });
        }.bind(this));
        Promise.all(promiseList).then(function(o) {
            console.log("creating collection from booster");
            var collection = new fabric.CardCollection(o,{top: this.top, left: this.left});
            this.canvas.add(collection);
            this.canvas.renderAll();
            this.remove();
            collection.id = this.id;
            if (callback) callback(collection);
        }.bind(this));
    },
    toObject: function(propertiesToInclude) {
        return this.callSuper("toObject",["cards","id","boosterSet"].concat(propertiesToInclude));
    },
});

//Static
fabric.BoosterPack.fromObject = function(object,callback) {
    var key = object.boosterSet.toLowerCase();
    fabric.util.loadImage("http://localhost:3000/boosters/"+key+".png", function(img) {
    
        callback && callback(new fabric.BoosterPack(object.cards, _.extend({},object,{boosterImage: img})));
    }, null, null);
}

fabric.BoosterPack.forSet = function(set,object,callback) {
    CardService.generateBooster(set).then(function(cards) {
        _.each(cards,(c) => c.fabricId = Util.guid());
        fabric.util.loadImage("http://localhost:3000/boosters/"+set.toLowerCase()+".png", function(img) {
            var booster = new fabric.BoosterPack(cards, _.extend({},object,{boosterImage: img, boosterSet: set}));
            if (callback) callback(booster);
        }, this, true);
    }.bind(this));
}

fabric.BoosterPack.async = true; //This makes object loading asynchronous, similar to image

export default fabric.BoosterPack;

