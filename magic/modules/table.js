import Util from "./util";
import FabricUtil from "./fabricUtil";
import CardService from "./cardService";
import Card from "./Card";
import CardCollection from "./CardCollection";

var EventEmitter = require("events").EventEmitter;
var fabric = require('fabric').fabric;
var _ = require("lodash");
var fetch = require('node-fetch');

class Table extends EventEmitter {
    constructor(canvas) {
        super();
        this.canvas = canvas;
        canvas.selection = false;
        this.objectsById = {};
    }
    generateBooster(code) {
        var order = {};
        CardService.generateBooster(code).then(function(cards) {
            var promiseList = _.map(cards,function(card,index) {
                return this.spawnCard(card).then(function(o) {
                    o.set({left: 10, top: index*35});
                    o.setCoords();
                    order[o.id] = index;
                    return o;
                }.bind(this));
            }.bind(this));
            var promise = Promise.all(promiseList);
            promise.then(function(o) {
                this.restack(order);
                //this.createCollection(o,"collection");
            }.bind(this));
        }.bind(this));
    }
    spawnCard(card) {
        return new Promise(function(resolve,reject) {
            fabric.Card.fromMetadata(card, function(o) {
                this.canvas.add(o);
                this.regenerateObjectsById();
                resolve(o);
            }.bind(this));
        }.bind(this));
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
        this.canvas.renderAll();
    }
    onCreateCollection(e) {
        var cards = _.map(e.cards,(id) => this.objectsById[id]);
        delete e.type;
        var collection = new fabric.CardCollection(cards,e);
        console.log("created collection",collection.id);
        this.canvas.add(collection);
        this.regenerateObjectsById();
    }
    onDismantleCollection(e) {
        var collection = this.objectsById[e.id];
        if (!collection || collection.type != "cardCollection") return console.log("FAILED to dismantle collection",e.id);
        collection.dismantle();
        console.log("dismantle collection",e.id);
        this.regenerateObjectsById();
    }
    onAddToCollection(e) {
        var collection = this.objectsById[e.id];
        var cards = _.map(e.cards,(id) => this.objectsById[id]);
        collection.insertCards(cards,e.index);
    }
    onRemoveFromCollection(e) {
        var object = this.objectsById[e.id];
        var collection = object.group;
        collection.removeCard(object);
        this.regenerateObjectsById();
    }
    addToCollection(group,o) {
        var n = group.getObjects().length;
        var groupKind = group.kind;
        var groupId = group.id;

        var objects = group._objects;
        group._restoreObjectsState();
        canvas.remove(group);
        _.each(objects,function(o) {
            this.canvas.add(o);
        }.bind(this));

        if (o != null) {
            if (o.kind == "collection") {
                var objects = o._objects;
                o._restoreObjectsState();
                this.canvas.remove(o);
                _.each(objects,function(o) {
                    this.canvas.add(o);
                    objects.push(o);
                }.bind(this));
            } else {
                objects.push(o);
            }
        }

        var group = this.createCollection(canvas,objects,groupKind);
        group.id = groupId;
    }
}

export default Table;

