import Util from "./util";
import FabricUtil from "./fabricUtil";
import CardService from "./cardService";
import Card from "./Card";
import CardCollection from "./CardCollection";
import BoosterPack from "./BoosterPack";
import EventFactory from "./EventFactory";
import constants from "./constants";

var EventEmitter = require("events").EventEmitter;
var fabric = require('fabric').fabric;
var _ = require("lodash");
var fetch = require('node-fetch');

class Table extends EventEmitter {
    constructor(canvas,emit) {
        super();
        this.emit = emit;
        this.canvas = canvas;
        canvas.selection = false;
        this.objectsById = {};
    }
    loadFromJSON(data) {
        this.canvas.loadFromJSON(data, function () {
            this.regenerateObjectsById();
            this.canvas.renderAll();
        }.bind(this));
    }
    restack(order) {
        var arr = _(order).toPairs().sortBy(1).value();
        var objById = {};
        _.each(this.canvas.getObjects(),function(o) {
            objById[o.id] = o;
        });

        _.each(arr,function(v,index) {
            objById[v[0]].moveTo(index);
        });
    }
    regenerateObjectsById() {
        this.objectsById = _.reduce(this.canvas.getObjects(),function(acc,o) { acc[o.id] = o; return acc },{});
    }
    processEvent(e) {
        var type = e.type;
        var fname = "on"+Util.capitalize(e.type);
        this[fname](e);
    }
    onObjectsCreated(e) {
        fabric.util.enlivenObjects(e.objects, function (enlivenedObjects) {
            _.each(enlivenedObjects,o => this.canvas.add(o));
            this.regenerateObjectsById();
        }.bind(this));
    }
    onOpenBooster(e) {
        var o = this.objectsById[e.id];
        o.open(function(collection) {
            this.regenerateObjectsById();
        }.bind(this));
    }
    onUpdate(e) {
        var id = e.id;
        var o = this.objectsById[id];
        if (!o) {
            console.log("unknown object: ",id);
            console.log("objs: ",_.keys(this.objectsById));
            return;
        }
        delete e.id;
        delete e.type;
        _.extend(o,e);
        o.setCoords();
        o.bringToFront();
        this.canvas.renderAll();
    }
    onSpawnBooster(e) {
        console.log("spawning booster",e);
        var set = e.set;
        fabric.BoosterPack.forSet(e.set,{x: e.x, y: e.y},function(booster) {
            this.canvas.add(booster);
            this.regenerateObjectsById();
            this.emit(EventFactory.objectsCreated([booster]));
        }.bind(this));
    }
    onMergeIntoCollection(e) {
        var objects = _.map(e.objects,(id) => this.objectsById[id]);
        delete e.type;
        delete e.objects;
        var collection = new fabric.CardCollection(objects,e);
        console.log("merged into collection",collection.id);
        this.canvas.add(collection);
        this.regenerateObjectsById();
        this.canvas.renderAll();
    }
    onCreateCollection(e) {
        var cards = _.map(e.cards,(id) => this.objectsById[id]);
        delete e.type;
        delete e.cards;
        var collection = new fabric.CardCollection(cards,e);
        console.log("created collection",collection.id);
        this.canvas.add(collection);
        this.regenerateObjectsById();
        this.canvas.renderAll();
    }
    onDismantleCollection(e) {
        var collection = this.objectsById[e.id];
        if (!collection || collection.type != "cardCollection") return console.log("FAILED to dismantle collection",e.id);
        collection.dismantle();
        console.log("dismantle collection",e.id);
        this.regenerateObjectsById();
        this.canvas.renderAll();
    }
    onAddToCollection(e) {
        var collection = this.objectsById[e.id];
        console.log("adding to collection",e.cards);
        var cards = _.map(e.cards,(id) => this.objectsById[id]);
        collection.insertCards(cards,e.index);
        this.canvas.renderAll();
    }
    onRemoveFromCollection(e) {
        var collection = this.objectsById[e.collectionId];
        var card = _.find(collection.getObjects(),{id:e.id});
        console.log("remove card being called");
        collection.removeCard(card);
        this.regenerateObjectsById();
        this.canvas.renderAll();
    }
}

export default Table;

